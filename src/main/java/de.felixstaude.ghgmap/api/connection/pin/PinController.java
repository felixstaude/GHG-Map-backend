package de.felixstaude.ghgmap.api.connection.pin;

import de.felixstaude.ghgmap.file.ImageProcessor;
import de.felixstaude.ghgmap.nominatim.NominatimClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/pin")
public class PinController {

    @Autowired
    private ImageProcessor imageProcessor;

    @PostMapping("/add")
    public Map<String, Object> addPin(
            @RequestPart("json") String json,
            @RequestPart("image") MultipartFile image) {

        ObjectMapper objectMapper = new ObjectMapper();
        PinRequest pinRequest;
        try {
            pinRequest = objectMapper.readValue(json, PinRequest.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }

        int pinId = getNextPinId();
        String fileExtension = "";

        String relativeFilePath;
        try {
            String originalFilename = image.getOriginalFilename();
            if (originalFilename != null) {
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex >= 0) {
                    fileExtension = originalFilename.substring(dotIndex + 1).toLowerCase();
                }
            }

            if (!fileExtension.equals("png") && !fileExtension.equals("jpg") && !fileExtension.equals("jpeg")) {
                throw new RuntimeException("Unsupported image format: " + fileExtension);
            }

            relativeFilePath = "data/images/" + ZonedDateTime.now().getYear() + "/"
                    + ZonedDateTime.now().getMonthValue() + "/" + ZonedDateTime.now().getDayOfMonth() + "/" + pinId + "." + fileExtension;

            File imageFile = new File(relativeFilePath);
            if (!imageFile.getParentFile().exists()) {
                imageFile.getParentFile().mkdirs();
            }

            imageProcessor.saveImage(image.getBytes(), imageFile.getAbsolutePath());

            // Speichere die Pin-Daten mit der übergebenen town Information
            try (FileWriter writer = new FileWriter("data/pins.csv", true)) {
                String town = pinRequest.getTown();
                if (town == null || town.isEmpty()) {
                    // Falls town nicht übergeben wurde, verwende Nominatim zur Bestimmung der town
                    town = NominatimClient.getTownFromCoordinates(
                            Double.parseDouble(pinRequest.getLat()),
                            Double.parseDouble(pinRequest.getLng()));
                }

                writer.append(String.valueOf(pinId))
                        .append(';')
                        .append(pinRequest.getUserId())
                        .append(';')
                        .append(pinRequest.getDescription())
                        .append(';')
                        .append(pinRequest.getLat())
                        .append(';')
                        .append(pinRequest.getLng())
                        .append(';')
                        .append(town)
                        .append(';')
                        .append(relativeFilePath.replace("data/images/", ""))
                        .append('\n');
            }

            updateStatistics();

        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }

        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/" + relativeFilePath)
                .toUriString();

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("pinId", pinId);
        response.put("lat", pinRequest.getLat());
        response.put("lng", pinRequest.getLng());
        response.put("description", pinRequest.getDescription());
        response.put("town", pinRequest.getTown()); // Füge die town zur Antwort hinzu
        response.put("imageUrl", imageUrl);
        return response;
    }

    private void updateStatistics() {
        String today = ZonedDateTime.now().getYear() + ";" +
                String.format("%02d", ZonedDateTime.now().getMonthValue()) + ";" +
                String.format("%02d", ZonedDateTime.now().getDayOfMonth());

        File statsFile = new File("data/stats.csv");
        try {
            if (!statsFile.exists()) {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
            }

            List<String> lines = Files.readAllLines(statsFile.toPath());
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(";");
                if ((parts[0] + ";" + parts[1] + ";" + parts[2]).equals(today)) {
                    // Anzahl für den aktuellen Tag erhöhen
                    int count = Integer.parseInt(parts[3]) + 1;
                    lines.set(i, today + ";" + count);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                lines.add(today + ";1");
            }

            Files.write(statsFile.toPath(), lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to update statistics", e);
        }
    }


    @GetMapping("/get/all")
    public Map<String, Map<String, Object>> getAllPins() {
        Map<String, Map<String, Object>> pins = new HashMap<>();
        try (Scanner scanner = new Scanner(new File("data/pins.csv"))) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(";");
                Map<String, Object> pinData = new HashMap<>();

                pinData.put("lat", Double.parseDouble(values[3]));
                pinData.put("lng", Double.parseDouble(values[4]));
                pins.put(values[0], pinData);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pins", e);
        }
        return pins;
    }

    @GetMapping("/get/data")
    public Map<String, Object> getPinData(@RequestParam("pinId") int pinId) {
        Map<String, Object> pinData = new HashMap<>();
        try (Scanner scanner = new Scanner(new File("data/pins.csv"))) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(";");
                int id = Integer.parseInt(values[0]);
                if (id == pinId) {
                    pinData.put("pinId", id);
                    pinData.put("userId", values[1]);
                    pinData.put("description", values[2]);
                    pinData.put("lat", values[3]);
                    pinData.put("lng", values[4]);
                    pinData.put("town", values[5]); // Füge town zur Antwort hinzu
                    pinData.put("imagePath", values[6]);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pin data", e);
        }
        return pinData;
    }

    private int getNextPinId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File("data/pins.csv"))) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(";");
                int id = Integer.parseInt(values[0]);
                if (id > maxId) {
                    maxId = id;
                }
            }
        } catch (IOException e) {
            // File not found or empty, return 0 as default
        }
        return maxId + 1;
    }
}

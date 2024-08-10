package de.felixstaude.ghgmap.api.connection.pin;

import de.felixstaude.ghgmap.file.ImageProcessor;
import de.felixstaude.ghgmap.nominatim.NominatimClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
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

            String creationDate = ZonedDateTime.now().toString();

            try (FileWriter writer = new FileWriter("data/pins.csv", true)) {
                String town = pinRequest.getTown();
                if (town == null || town.isEmpty()) {
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
                        .append(';')
                        .append("false")
                        .append(';')
                        .append(creationDate)
                        .append('\n');
                System.out.println("created new Pin:");
                System.out.println("PinID: " + pinId);
                System.out.println("UserID: " + pinRequest.getUserId());
                System.out.println("Town: " + pinRequest.getTown());
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
        response.put("town", pinRequest.getTown());
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
                if ("true".equals(values[7])) { // Check if the pin is approved
                    Map<String, Object> pinData = new HashMap<>();
                    pinData.put("lat", Double.parseDouble(values[3]));
                    pinData.put("lng", Double.parseDouble(values[4]));
                    pins.put(values[0], pinData);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pins", e);
        }
        return pins;
    }



    @GetMapping("/admin/all/approved")
    public ResponseEntity<Map<String, Object>> getAllApprovedPinsAdmin(@RequestHeader("access_token") String authorizationHeader,
                                                               @RequestParam("userId") String userId) {
        String accessToken = authorizationHeader.replace("Bearer ", "");

        if (!isTokenValidForUser(accessToken, userId) || !isAdminUser(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("ok", false);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        List<Map<String, Object>> pins = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("data/pins.csv"))) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(";");
                Map<String, Object> pinData = new HashMap<>();
                if(values[7].equalsIgnoreCase("true")){
                    pinData.put("pinId", values[0]);
                    pinData.put("userId", values[1]);
                    pinData.put("description", values[2]);
                    pinData.put("lat", values[3]);
                    pinData.put("lng", values[4]);
                    pinData.put("town", values[5]);
                    pinData.put("imagePath", values[6]);
                    pinData.put("approved", values[7]);
                    pins.add(pinData);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pins", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("pins", pins);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/admin/all/unapproved")
    public ResponseEntity<Map<String, Object>> getAllUnapprovedPinsAdmin(@RequestHeader("access_token") String authorizationHeader,
                                                               @RequestParam("userId") String userId) {
        String accessToken = authorizationHeader.replace("Bearer ", "");

        if (!isTokenValidForUser(accessToken, userId) || !isAdminUser(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("ok", false);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        List<Map<String, Object>> pins = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("data/pins.csv"))) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(";");
                Map<String, Object> pinData = new HashMap<>();
                if(values[7].equalsIgnoreCase("false")){
                    pinData.put("pinId", values[0]);
                    pinData.put("userId", values[1]);
                    pinData.put("description", values[2]);
                    pinData.put("lat", values[3]);
                    pinData.put("lng", values[4]);
                    pinData.put("town", values[5]);
                    pinData.put("imagePath", values[6]);
                    pinData.put("approved", values[7]);
                    pins.add(pinData);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pins", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("pins", pins);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/admin/approve")
    public ResponseEntity<Map<String, Object>> approvePin(@RequestHeader("access_token") String authorizationHeader,
                                                          @RequestParam("userId") String userId,
                                                          @RequestParam("pinId") int pinId) {
        String accessToken = authorizationHeader.replace("Bearer ", "");

        if (!isTokenValidForUser(accessToken, userId) || !isAdminUser(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("ok", false);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        boolean found = false;
        File pinsFile = new File("data/pins.csv");
        List<String> lines;
        try {
            lines = Files.readAllLines(pinsFile.toPath());
            for (int i = 0; i < lines.size(); i++) {
                String[] values = lines.get(i).split(";");
                int id = Integer.parseInt(values[0]);
                if (id == pinId) {
                    values[7] = "true"; // Approve the pin
                    lines.set(i, String.join(";", values));
                    found = true;
                    break;
                }
            }
            Files.write(pinsFile.toPath(), lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update pin approval status", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ok", found);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/get/data")
    public Map<String, Object> getPinData(@RequestParam("pinId") int pinId) {
        Map<String, Object> pinData = new HashMap<>();
        try (Scanner scanner = new Scanner(new File("data/pins.csv"))) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(";");
                int id = Integer.parseInt(values[0]);
                if (id == pinId && "true".equals(values[7])) {
                    pinData.put("pinId", id);
                    pinData.put("userId", values[1]);
                    pinData.put("description", values[2]);
                    pinData.put("lat", values[3]);
                    pinData.put("lng", values[4]);
                    pinData.put("town", values[5]);
                    pinData.put("imagePath", values[6]);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pin data", e);
        }
        return pinData;
    }

    @GetMapping("/admin/get")
    public ResponseEntity<Map<String, Object>> getPinDataAdmin(@RequestHeader("access_token") String authorizationHeader,
                                                               @RequestParam("userId") String userId,
                                                               @RequestParam("pinId") int pinId) {
        String accessToken = authorizationHeader.replace("Bearer ", "");

        if (!isTokenValidForUser(accessToken, userId) || !isAdminUser(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("ok", false);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

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
                    pinData.put("town", values[5]);
                    pinData.put("imagePath", values[6]);
                    pinData.put("approved", values[7]);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pin data", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("pinData", pinData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/admin/delete")
    public ResponseEntity<Map<String, Object>> deletePin(@RequestHeader("access_token") String authorizationHeader,
                                                         @RequestParam("userId") String userId,
                                                         @RequestParam("pinId") int pinId) {
        String accessToken = authorizationHeader.replace("Bearer ", "");

        if (!isTokenValidForUser(accessToken, userId) || !isAdminUser(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("ok", false);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        File pinsFile = new File("data/pins.csv");
        List<String> updatedLines = new ArrayList<>();
        boolean pinFound = false;
        String[] deletedPinData = null;

        try (Scanner scanner = new Scanner(pinsFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(";");
                int id = Integer.parseInt(values[0]);

                if (id == pinId) {
                    pinFound = true;
                    deletedPinData = values;
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pins data", e);
        }

        if (pinFound) {
            try {
                Files.write(pinsFile.toPath(), updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                if (deletedPinData != null) {
                    String imagePath = "data/images/" + deletedPinData[6];
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        if (!imageFile.delete()) {
                            throw new RuntimeException("Failed to delete image file: " + imagePath);
                        }
                    }

                    updateStatisticsOnDelete(deletedPinData);
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to update pins data", e);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ok", pinFound);
        if (pinFound) {
            response.put("message", "Pin deleted successfully.");
        } else {
            response.put("message", "Pin not found.");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    private void updateStatisticsOnDelete(String[] pinData) {
        String creationDate = pinData[8];
        String[] dateParts = creationDate.split("T")[0].split("-"); // Nur das Datum extrahieren
        String targetDate = dateParts[0] + ";" + dateParts[1] + ";" + dateParts[2];

        File statsFile = new File("data/stats.csv");

        try {
            List<String> lines = Files.readAllLines(statsFile.toPath());
            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                String[] parts = line.split(";");
                String statDate = parts[0] + ";" + parts[1] + ";" + parts[2];
                if (statDate.equals(targetDate)) {
                    int count = Integer.parseInt(parts[3]) - 1;
                    if (count > 0) {
                        updatedLines.add(statDate + ";" + count);
                    }
                } else {
                    updatedLines.add(line);
                }
            }

            Files.write(statsFile.toPath(), updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to update statistics", e);
        }
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
            return 0;
        }
        return maxId + 1;
    }

    private boolean isTokenValidForUser(String accessToken, String userId) {
        String url = "https://id.twitch.tv/oauth2/validate";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "OAuth " + accessToken);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new org.springframework.http.HttpEntity<>(headers), Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                String tokenUserId = (String) body.get("user_id");
                return userId.equals(tokenUserId);
            }
        } catch (HttpClientErrorException e) {
            // Token ist ungültig oder es gab einen Fehler
            return false;
        }
        return false;
    }

    private boolean isAdminUser(String userId) {
        File adminFile = new File("data/admin_users.csv");
        if (!adminFile.exists()) {
            return false;
        }

        try (Scanner scanner = new Scanner(adminFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.equals(userId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read admin users file", e);
        }

        return false;
    }


}

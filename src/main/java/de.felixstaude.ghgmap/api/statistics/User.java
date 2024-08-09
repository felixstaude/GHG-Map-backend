package de.felixstaude.ghgmap.api.statistics;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class User {

    public static Map<String, Object> getPinsByUserId(String userId) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> userPins = new ArrayList<>();

        File pinsFile = new File("data/pins.csv");
        if (!pinsFile.exists()) {
            response.put("ok", false);
            return response;
        }

        try (Scanner scanner = new Scanner(pinsFile)) {
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(";");
                if (values[1].equals(userId)) {
                    Map<String, Object> pinData = new HashMap<>();
                    pinData.put("pinId", Integer.parseInt(values[0]));
                    pinData.put("description", values[2]);
                    pinData.put("lat", Double.parseDouble(values[3]));
                    pinData.put("lng", Double.parseDouble(values[4]));
                    pinData.put("imagePath", values[5]);
                    userPins.add(pinData);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read pins data", e);
        }

        if (userPins.isEmpty()) {
            response.put("ok", false);
        } else {
            response.put("ok", true);
            response.put("pins", userPins);
        }

        return response;
    }
}

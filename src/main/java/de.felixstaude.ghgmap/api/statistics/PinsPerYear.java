package de.felixstaude.ghgmap.api.statistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class PinsPerYear {

    public static Integer getPinsForSpecificYear(String year) {
        // TODO pins nur zählen lassen wenn approved
        File statsFile = new File("data/stats.csv");
        if (!statsFile.exists()) {
            return 0;
        }

        int totalPins = 0;

        try {
            List<String> lines = Files.readAllLines(statsFile.toPath());

            for (String line : lines) {
                if (line.startsWith(year)) {
                    String[] parts = line.split(";");
                    totalPins += Integer.parseInt(parts[3]);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read statistics", e);
        }

        return totalPins;
    }
}

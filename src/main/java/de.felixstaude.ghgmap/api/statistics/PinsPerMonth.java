package de.felixstaude.ghgmap.api.statistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class PinsPerMonth {

    public static Integer getPinsForSpecificMonth(String year, String month) {
        String targetDate = year + ";" + month;

        File statsFile = new File("data/stats.csv");
        if (!statsFile.exists()) {
            return 0;
        }

        int totalPins = 0;

        try {
            List<String> lines = Files.readAllLines(statsFile.toPath());

            for (String line : lines) {
                if (line.startsWith(targetDate)) {
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

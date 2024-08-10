package de.felixstaude.ghgmap.api.statistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.List;

public class PinsPerDay {
 // TODO nur die pins zählen die approved wurden
    public static Integer getPinsToday() {
        String today = ZonedDateTime.now().getYear() + ";" +
                String.format("%02d", ZonedDateTime.now().getMonthValue()) + ";" +
                String.format("%02d", ZonedDateTime.now().getDayOfMonth());

        return getPinsForSpecificDay(
                String.valueOf(ZonedDateTime.now().getYear()),
                String.format("%02d", ZonedDateTime.now().getMonthValue()),
                String.format("%02d", ZonedDateTime.now().getDayOfMonth())
        );
    }

    public static Integer getPinsForSpecificDay(String year, String month, String day) {
        String targetDate = year + ";" + month + ";" + day;

        File statsFile = new File("data/stats.csv");
        if (!statsFile.exists()) {
            return 0;
        }

        try {
            List<String> lines = Files.readAllLines(statsFile.toPath());

            for (String line : lines) {
                String[] parts = line.split(";");
                if ((parts[0] + ";" + parts[1] + ";" + parts[2]).equals(targetDate)) {
                    return Integer.parseInt(parts[3]);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read statistics", e);
        }

        return 0;
    }
}

package de.felixstaude.ghgmap.file;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class ImageProcessor {

    public void saveImage(byte[] imageBytes, String fileName) throws IOException {
        File directory = new File("");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(imageBytes);
            fileOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }
}

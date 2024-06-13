package de.felixstaude.ghgmap.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageProcessor {

    public void saveImage(String base64Image, String pinId) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        File directory = new File("src/main/resources/images/");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, pinId + ".png");

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(decodedBytes);
            fileOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }
}

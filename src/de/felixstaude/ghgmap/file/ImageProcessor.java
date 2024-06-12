package de.felixstaude.ghgmap.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageProcessor {

    public static void saveImage(String image, String pinId){
        File directory = new File("resources/images/");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, pinId + ".txt");

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            if(file.exists()){
                file.delete();
            }

            fileOutputStream.write(image.getBytes());
            fileOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

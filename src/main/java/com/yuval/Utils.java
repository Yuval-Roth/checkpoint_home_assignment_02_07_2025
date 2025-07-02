package com.yuval;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    public static void appendToFile(String filePath, String content) {
        File file = new File(filePath);
        file.mkdirs();
//        try {
//            file.createNewFile();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to file: " + filePath, e);
        }
    }
}

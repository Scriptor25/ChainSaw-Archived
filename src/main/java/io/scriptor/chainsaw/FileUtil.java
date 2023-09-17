package io.scriptor.chainsaw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {

    private FileUtil() {
    }

    public static String readFile(String path) {
        String source = "";
        try (var reader = new BufferedReader(new FileReader(path))) {

            var builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line).append('\n');

            source = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source;
    }
}

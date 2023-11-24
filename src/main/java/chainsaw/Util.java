package chainsaw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Util {

    private Util() {
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

    public static <T> T error(String fmt, Object... args) {
        System.out.printf("Error: %s%n", String.format(fmt, args));
        return null;
    }
}

package io.scriptor.chainsaw.runtime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileStream implements Closeable {

    private BufferedWriter writer;
    private BufferedReader reader;

    public FileStream(String path, String mode) throws IOException {
        switch (mode) {
            case "out-app":
                writer = new BufferedWriter(new FileWriter(path, true));
                break;
            case "out":
                writer = new BufferedWriter(new FileWriter(path, false));
                break;
            case "in":
                reader = new BufferedReader(new FileReader(path));
                break;
            default:
                throw new RuntimeException("undefined file stream mode '" + mode + "'");
        }
    }

    public void out(String str) throws IOException {
        writer.append(str);
    }

    public String in() throws IOException {
        return reader.readLine();
    }

    @Override
    public void close() throws IOException {
        if (writer != null)
            writer.close();
        if (reader != null)
            reader.close();
    }

}

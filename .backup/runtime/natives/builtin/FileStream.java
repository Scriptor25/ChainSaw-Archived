package io.scriptor.chainsaw.runtime.natives.builtin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.natives.CSawParam;
import io.scriptor.chainsaw.runtime.natives.CSawType;

@CSawType(alias = "file")
public class FileStream implements Closeable {

    @CSawFunction
    public static FileStream file(@CSawParam String path, @CSawParam String mode) throws IOException {
        return new FileStream(path, mode);
    }

    private transient BufferedWriter writer;
    private transient BufferedReader reader;

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

    @CSawFunction
    public void out(@CSawParam String fmt, @CSawParam Object... args) throws IOException {
        writer.append(String.format(fmt, args));
    }

    @CSawFunction
    public String in() throws IOException {
        return reader.readLine();
    }

    @Override
    @CSawFunction
    public void close() throws IOException {
        if (writer != null)
            writer.close();
        if (reader != null)
            reader.close();
    }

}

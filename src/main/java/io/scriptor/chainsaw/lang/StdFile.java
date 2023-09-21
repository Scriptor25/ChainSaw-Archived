package io.scriptor.chainsaw.lang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import io.scriptor.chainsaw.runtime.natives.CSawNative;

@CSawNative(alias = "file")
public class StdFile {

    private BufferedReader mReader = null;
    private BufferedWriter mWriter = null;

    public StdFile(String name, String mode) throws IOException {
        this(new File(name), mode);
    }

    private StdFile(File file, String mode) throws IOException {
        switch (mode) {
            case "in":
                mReader = new BufferedReader(new FileReader(file));
                break;
            case "out":
                mWriter = new BufferedWriter(new FileWriter(file));
                break;
        }
    }

    public void out(String fmt, Object... args) throws IOException {
        mWriter.write(String.format(fmt, args));
    }

    public void close() throws IOException {
        if (mReader != null)
            mReader.close();
        if (mWriter != null)
            mWriter.close();
    }
}

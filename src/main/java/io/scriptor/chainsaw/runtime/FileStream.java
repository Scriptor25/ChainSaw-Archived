package io.scriptor.chainsaw.runtime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileStream {

    private BufferedReader mReader = null;
    private BufferedWriter mWriter = null;

    public FileStream(String name, String mode) throws IOException {
        switch (mode) {
            case "in":
                mReader = new BufferedReader(new FileReader(name));
                break;
            case "out":
                mWriter = new BufferedWriter(new FileWriter(name));
                break;
        }

    }

    public void out(String str) throws IOException {
        mWriter.write(str);
    }

    public void close() throws IOException {
        if (mReader != null)
            mReader.close();
        if (mWriter != null)
            mWriter.close();
    }
}

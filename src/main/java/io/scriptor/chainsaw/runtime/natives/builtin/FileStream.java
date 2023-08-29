package io.scriptor.chainsaw.runtime.natives.builtin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.natives.CSawType;
import io.scriptor.chainsaw.runtime.type.NativeType;
import io.scriptor.chainsaw.runtime.value.NativeValue;
import io.scriptor.chainsaw.runtime.value.StringValue;
import io.scriptor.chainsaw.runtime.value.Value;

@CSawType(alias = "file")
public class FileStream implements Closeable {

    @CSawFunction(id = "file", constructor = true, result = "file", params = { "path: str", "mode: str" })
    public static Value file(Environment env, Map<String, Value> params) throws IOException {
        return new NativeValue<>(env, NativeType.get(env, "file"),
                new FileStream((String) params.get("path").getValue(), (String) params.get("mode").getValue()));
    }

    @CSawFunction(id = "out", params = { "fmt: str" }, vararg = true, memberOf = "file")
    public static Value out(Environment env, Map<String, Value> params) throws IOException {
        var file = (FileStream) params.get("my").getValue();
        var fmt = (String) params.get("fmt").getValue();

        Object[] args = new Object[params.size() - 2];
        for (int i = 0; i < args.length; i++)
            args[i] = params.get("vararg" + i);

        file.out(String.format(fmt, args));

        return null;
    }

    @CSawFunction(id = "in", result = "str", memberOf = "file")
    public static Value in(Environment env, Map<String, Value> params) throws IOException {
        return new StringValue(env, ((FileStream) params.get("my").getValue()).in());
    }

    @CSawFunction(id = "close", memberOf = "file")
    public static Value close(Environment env, Map<String, Value> params) throws IOException {
        var file = (FileStream) params.get("my").getValue();

        file.close();

        return null;
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

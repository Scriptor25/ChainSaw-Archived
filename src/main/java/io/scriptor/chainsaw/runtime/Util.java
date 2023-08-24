package io.scriptor.chainsaw.runtime;

public class Util {

    private Util() {
    }

    private static final boolean FATAL_ERROR = false;

    public static <T> T error(String fmt, Object... args) {
        var err = String.format(fmt, args);

        if (FATAL_ERROR)
            throw new RuntimeException(err);

        System.err.println(err);
        return null;
    }

    public static String format(String fmt, Object... args) {
        return String.format(fmt.replaceAll("%r", "\r"), args);
    }
}

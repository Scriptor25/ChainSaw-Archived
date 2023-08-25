package io.scriptor.chainsaw.runtime;

public class Util {

    private Util() {
    }

    private static final boolean FATAL_ERROR = true;

    public static <T> T error(String fmt, Object... args) {
        System.out.println();
        var err = String.format(fmt, args);
        System.err.println(err);

        if (FATAL_ERROR)
            throw new RuntimeException(err);

        return null;
    }

    public static String format(String fmt, Object... args) {
        return String.format(fmt.replaceAll("%r", "\r"), args);
    }
}

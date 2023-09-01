package io.scriptor.chainsaw.runtime;

public class Error {

    private Error() {
    }

    public static <T> T error(String fmt, Object... args) {
        System.out.printf("Error: %s%n", String.format(fmt, args));
        System.exit(1);
        return null;
    }
}

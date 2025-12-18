package java.lang;

/**
 * RoboVM stub for Boolean.hashCode(boolean) method added in Java 8/9
 * that Kotlin reflection uses but RoboVM doesn't have.
 */
public final class Boolean {
    /**
     * Returns a hash code for a boolean value.
     * Compatible with the static method added in Java 8.
     */
    public static int hashCode(boolean value) {
        return value ? 1231 : 1237;
    }
}

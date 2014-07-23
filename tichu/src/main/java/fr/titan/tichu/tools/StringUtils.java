package fr.tichu.tools;

/**
 *
 */
public class StringUtils {

    public static boolean isEmpty(String value) {
        return value == null || "".equals(value.trim());
    }
}

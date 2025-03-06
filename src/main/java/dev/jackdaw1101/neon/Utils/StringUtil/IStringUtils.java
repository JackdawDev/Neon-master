package dev.jackdaw1101.neon.Utils.StringUtil;

public class IStringUtils {

    // Check if a string is empty or null
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    // Check if a string is not empty
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    // Capitalize the first letter of a string
    public static String capitalize(String str) {
        if (isEmpty(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Reverse a string
    public static String reverse(String str) {
        if (isEmpty(str)) return str;
        return new StringBuilder(str).reverse().toString();
    }

    // Checks if two strings are equal (null-safe)
    public static boolean equals(String str1, String str2) {
        return str1 != null && str1.equals(str2);
    }

    // Remove all spaces from a string
    public static String removeSpaces(String str) {
        if (isEmpty(str)) return str;
        return str.replace(" ", "");
    }
}


package dev.jackdaw1101.neon.Utils.StringUtil;

public class IStringUtils {


    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }


    public static String capitalize(String str) {
        if (isEmpty(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    public static String reverse(String str) {
        if (isEmpty(str)) return str;
        return new StringBuilder(str).reverse().toString();
    }


    public static boolean equals(String str1, String str2) {
        return str1 != null && str1.equals(str2);
    }


    public static String removeSpaces(String str) {
        if (isEmpty(str)) return str;
        return str.replace(" ", "");
    }
}


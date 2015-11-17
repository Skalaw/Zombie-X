package com.asda.zombiex.utils;

/**
 * @author Skala
 */
public class StringUtils {

    public static String append(String... params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
        }

        return sb.toString();
    }
}

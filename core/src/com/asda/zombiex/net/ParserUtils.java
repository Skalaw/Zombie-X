package com.asda.zombiex.net;

import com.badlogic.gdx.utils.Array;

/**
 * @author Skala
 */
public class ParserUtils {

    public static Array<String> splitRequest(String response) {
        return splitString(response, true);
    }

    public static Array<String> splitResponse(String response) {
        return splitString(response, false);
    }

    private static Array<String> splitString(String input, boolean withoutLastChar) {
        Array<String> strings = new Array<String>();

        int wLastChar = withoutLastChar ? 1 : 0;

        int firstChar = 0;
        int lastChar = input.indexOf("|");
        while (lastChar != -1) {
            String splitStrings = input.substring(firstChar, lastChar + wLastChar);
            firstChar = lastChar + 1;
            lastChar = input.indexOf("|", firstChar);
            strings.add(splitStrings);
        }
        return strings;
    }
}

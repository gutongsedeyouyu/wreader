package org.wreader.reader.core.helper;

public class TextHelper {
    public static boolean isOccurenceTimeOdd(String text, int index) {
        char c = text.charAt(index);
        int occurrenceTime = 0;
        for (int i = 0; i <= index; i++) {
            if (text.charAt(i) == c) {
                occurrenceTime++;
            }
        }
        return occurrenceTime % 2 == 1;
    }

    public static boolean isSpace(char c) {
        return c == ' ' || c == '\u3000';
    }

    public static boolean isUtf16LeadSurrogate(char c) {
        return c >= '\ud800' && c <= '\udbff';
    }

    public static boolean isUtf16TrailSurrogate(char c) {
        return c >= '\udc00' && c <= '\udfff';
    }
}

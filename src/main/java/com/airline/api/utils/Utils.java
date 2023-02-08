package com.airline.api.utils;

public class Utils {
    public static String capitalizeFirstLetter(String originalString) {
        return originalString.substring(0, 1).toUpperCase() + originalString.substring(1);
    }
}

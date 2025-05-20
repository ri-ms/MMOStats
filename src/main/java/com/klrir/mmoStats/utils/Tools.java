package com.klrir.mmoStats.utils;

public class Tools {
    public static double round(double num, int digit) {
        double d = Math.pow(10, digit);

        return Math.round(num * d) / d;
    }
    public static String toShortNumber(double num) {
        String str;
        if (num > 999) {
            if (num > 9999) {
                if (num > 999999) {
                    if (num > 9999999) {
                        if (num > 999999999d) {
                            if (num > 9999999999d) str = (int) ((num / 1000000000)) + "b";
                            else str = round(num / 1000000000, 1) + "b";
                        } else str = (int) ((num / 1000000)) + "M";

                    } else str = round(num / 1000000, 1) + "M";
                } else str = (int) ((num / 1000)) + "k";
            } else str = round(num / 1000, 1) + "k";
        } else str = num + "";

        return str;
    }
}

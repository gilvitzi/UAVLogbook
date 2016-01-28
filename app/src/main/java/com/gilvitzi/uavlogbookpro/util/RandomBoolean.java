package com.gilvitzi.uavlogbookpro.util;

/**
 * Created by Gil on 27/01/2016.
 */
public class RandomBoolean {
    public static boolean get() {
        return Math.random() < 0.5;
    }

    public static boolean get(double chanceOfTrue)
    {
        return Math.random() < chanceOfTrue;
    }
}

package com.gilvitzi.uavlogbookpro.util;

/**
 * Created by Gil on 17/10/2015.
 */
public class StringValuePair extends ValuePair<String, String> {

    public StringValuePair(String first, String second) {
        super(first, second);
    }

    public StringValuePair()
    {
        first = "";
        second = "";
    }
}

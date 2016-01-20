package com.gilvitzi.uavlogbookpro.util;

/**
 * Created by Gil on 17/10/2015.
 */
public class NameValuePair extends StringValuePair {

    public NameValuePair(String name,String value)
    {
        super(name,value);
    }

    public NameValuePair()
    {
        super();
    }

    public String getName()
    {
        return getFirst();
    }

    public String getValue()
    {
        return getSecond();
    }

    public void setName(String name)
    {
        setFirst(name);
    }

    public void setValue(String value)
    {
        setSecond(value);
    }

}

package com.gilvitzi.uavlogbookpro.util;

/**
 * Created by Gil on 17/10/2015.
 */

public class ValuePair<F,S> {
    protected F first;
    protected S second;

    public ValuePair()
    {
        first = null;
        second = null;
    }
    public ValuePair(F first,S second)
    {
        this.first = first;
        this.second = second;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    @Override
    public String toString() {
        return super.toString()
                + "ValuePair< "
                + first.getClass().getName() + " " + first.toString()
                + " , "
                + second.getClass().getName() + " " + second.toString()
                + " >";
    }
}

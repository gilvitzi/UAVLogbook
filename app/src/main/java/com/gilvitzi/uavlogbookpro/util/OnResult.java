package com.gilvitzi.uavlogbookpro.util;

/**
 * Created by User on 12/21/2017.
 */

public interface OnResult<T> {
    void onResult(boolean success, T returnValue);
}

package io.irontest.models.assertion;

import io.irontest.models.Properties;

/**
 * Created by Zheng on 1/06/2016.
 */
public class IntegerEqualAssertionProperties extends Properties {
    private int number;

    public IntegerEqualAssertionProperties() {}

    public IntegerEqualAssertionProperties(int number) {
        this.number = number;
    }
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}

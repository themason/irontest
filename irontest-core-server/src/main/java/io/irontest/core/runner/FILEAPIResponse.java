package io.irontest.core.runner;

public class FILEAPIResponse {
    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString() {
        return "value: " + value;
    }
}

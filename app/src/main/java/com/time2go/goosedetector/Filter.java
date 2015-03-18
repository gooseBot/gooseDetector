package com.time2go.goosedetector;

public interface Filter {
    public void pushValue(int x);
    public void reset();
    public float getValue();
    public long getCount();
}
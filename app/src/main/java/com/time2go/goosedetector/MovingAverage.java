package com.time2go.goosedetector;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {
    private final Queue<Integer> window = new LinkedList<Integer>();
    private final Integer period;
    private int sum;

    public MovingAverage(Integer period) {
        this.period = period;
    }

    public void newNum(Integer num) {
        sum += num;
        window.add(num);
        if (window.size() > period) {
            sum -= window.remove();
        }
    }

    public double getAvg() {
        if (window.isEmpty()) return 0; // technically the average is undefined
        return sum / window.size();
    }

}
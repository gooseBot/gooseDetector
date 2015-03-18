package com.time2go.goosedetector;

import java.util.LinkedList;
import java.util.Queue;

public class Histogram {

    /**
     * Constructor which sets name, number of bins, and range.
     * @param nbins the number of bins the histogram should have. The
     * range specified by min and max will be divided up into this
     * many bins.
     * @param min the minimum of the range covered by the histogram bins
     * @param max the maximum value of the range covered by the histogram bins
     */

    // private data used internally by this class.
    private double[] m_hist;
    private double m_min;
    private double m_max;
    private int m_nbins;
    private int m_entries;
    private double m_overflow;
    private double m_underflow;

    private final Queue<Integer> window = new LinkedList<Integer>();
    private final Integer period;
    private int sum;

    public Histogram(int nbins, double min, double max, Integer period){
        m_nbins = nbins;
        m_min = min;
        m_max = max;
        m_hist = new double[m_nbins];
        m_underflow = 0;
        m_overflow = 0;
        this.period = period;
    }

    public void fill(double x){
        Integer num = (int)x;
        sum += num;
        window.add(num);

        //update the histogram
        BinInfo bin = findBin(x);
        // check the result of findBin in case it was an overflow or underflow
        if (bin.isUnderflow){
            m_underflow++;
        }
        if (bin.isOverflow){
            m_overflow++;
        }
        if (bin.isInRange){
            m_hist[bin.index]++;
        }
        m_entries++;

        if (window.size() > period) {
            bin = findBin(window.element().doubleValue());
            if (bin.isUnderflow){
                m_underflow--;
            }
            if (bin.isOverflow){
                m_overflow--;
            }
            if (bin.isInRange){
                m_hist[bin.index]--;
            }
            m_entries--;
            //now remove it from the rolling average queue head
            sum -= window.remove();
        }
    }

    private class BinInfo {
        public int index;
        public boolean isUnderflow;
        public boolean isOverflow;
        public boolean isInRange;
    }

    private BinInfo findBin(double x){
        BinInfo bin = new BinInfo();
        bin.isInRange = false;
        bin.isUnderflow = false;
        bin.isOverflow = false;
        if (x < m_min){
            bin.isUnderflow = true;
        }
        else if (x > m_max){
            bin.isOverflow = true;
        }
        else {
            double binWidth = (m_max - m_min)/m_nbins;
            for (int i=0; i<m_nbins; i++){
                double highEdge = m_min + (i+1) * binWidth;
                if (x <= highEdge) {
                    bin.isInRange = true;
                    bin.index = i;
                    break;
                }
            }
        }
        return bin;
    }

//    public void clear(){
//        for (int i=0; i<m_nbins; i++){
//            m_hist[i] = 0;
//        }
//    }

    public double mean(){
        double sum = 0;
        double binWidth = (m_max - m_min)/m_nbins;
        for (int i=0; i<m_nbins; i++){
            double binCentreValue = m_min + (i + 0.5) * binWidth;
            sum += m_hist[i] * binCentreValue;
        }
        return sum/(m_entries - m_overflow - m_underflow);
    }

    public double getAvg() {
        if (window.isEmpty()) return 0; // technically the average is undefined
        return sum / window.size();
    }

}
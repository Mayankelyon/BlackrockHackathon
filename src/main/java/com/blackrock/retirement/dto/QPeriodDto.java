package com.blackrock.retirement.dto;

/**
 * Fixed-amount period: when transaction date in [start,end], replace remanent with fixed.
 */
public class QPeriodDto {
    private double fixed;
    private String start;
    private String end;

    public double getFixed() {
        return fixed;
    }

    public void setFixed(double fixed) {
        this.fixed = fixed;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}

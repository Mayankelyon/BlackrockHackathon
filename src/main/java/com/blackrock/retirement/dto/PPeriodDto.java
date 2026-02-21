package com.blackrock.retirement.dto;

/**
 * Extra-amount period: when transaction date in [start,end], add extra to remanent.
 */
public class PPeriodDto {
    private double extra;
    private String start;
    private String end;

    public double getExtra() {
        return extra;
    }

    public void setExtra(double extra) {
        this.extra = extra;
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

package com.blackrock.retirement.dto;

/**
 * Evaluation period: sum remanents of transactions in [start,end].
 */
public class KPeriodDto {
    private String start;
    private String end;

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

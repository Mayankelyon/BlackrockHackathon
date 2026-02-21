package com.blackrock.retirement.dto;

/**
 * Performance report: time (HH:mm:ss.SSS or ms), memory (XXX.XX MB), threads.
 */
public class PerformanceResponseDto {
    private String time;
    private String memory;
    private int threads;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }
}

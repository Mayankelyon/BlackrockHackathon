package com.blackrock.retirement.service;

import com.blackrock.retirement.dto.PerformanceResponseDto;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PerformanceService {

    private static final AtomicLong START_NANOS = new AtomicLong(System.nanoTime());

    public PerformanceResponseDto report() {
        PerformanceResponseDto dto = new PerformanceResponseDto();
        long uptimeMs = (System.nanoTime() - START_NANOS.get()) / 1_000_000;
        long hours = uptimeMs / 3_600_000;
        long minutes = (uptimeMs % 3_600_000) / 60_000;
        long seconds = (uptimeMs % 60_000) / 1_000;
        long millis = uptimeMs % 1_000;
        dto.setTime(String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis));

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long totalMb = rt.totalMemory() / (1024 * 1024);
        dto.setMemory(String.format("%.2f MB", (double) usedMb));

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        dto.setThreads(threadBean.getThreadCount());
        return dto;
    }
}

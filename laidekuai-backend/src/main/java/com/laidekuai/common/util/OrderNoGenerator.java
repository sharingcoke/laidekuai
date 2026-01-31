package com.laidekuai.common.util;

import org.springframework.stereotype.Component;

/**
 * 订单号生成器（雪花算法简化版）
 *
 * 格式: 时间戳 + 机器ID + 序列号
 * 示例: 2026013112345678901
 *
 * @author Laidekuai Team
 */
@Component
public class OrderNoGenerator {

    /**
     * 起始时间戳 (2026-01-01 00:00:00)
     */
    private static final long EPOCH = 1735660800000L;

    /**
     * 机器ID（5位，支持0-31）
     */
    private final long machineId;

    /**
     * 序列号（12位，支持0-4095）
     */
    private long sequence = 0L;

    /**
     * 上次生成时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     */
    public OrderNoGenerator() {
        // 使用服务器IP最后一段作为机器ID（简化版）
        this.machineId = 1L;
    }

    /**
     * 生成订单号（线程安全）
     */
    public synchronized String generate() {
        long timestamp = System.currentTimeMillis() - EPOCH;

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，订单号生成失败");
        }

        // 同一毫秒内，序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 0xFFF; // 12位掩码
            if (sequence == 0) {
                // 序列号溢出，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 新毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 拼接订单号
        long orderId = (timestamp << 17)        // 时间戳左移17位
                | (machineId << 12)              // 机器ID左移12位
                | sequence;                      // 序列号

        return String.valueOf(orderId);
    }

    /**
     * 等待下一毫秒
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() - EPOCH;
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() - EPOCH;
        }
        return timestamp;
    }
}

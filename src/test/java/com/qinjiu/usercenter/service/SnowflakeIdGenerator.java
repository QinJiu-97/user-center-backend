package com.qinjiu.usercenter.service;

public class SnowflakeIdGenerator {
    // 机器id
    private long workerId;
    // 数据中心id
    private long datacenterId;
    // 序列号
    private long sequence = 0L;
    // 起始时间戳
    private long twepoch = 1288834974657L;
    // 机器id的位数
    private long workerIdBits = 5L;
    // 数据中心id的位数
    private long datacenterIdBits = 5L;
    // 机器id最大值
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    // 数据中心id最大值
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    // 序列号的位数
    private long sequenceBits = 12L;
    // 机器id移位
    private long workerIdShift = sequenceBits;
    // 数据中心id移位
    private long datacenterIdShift = sequenceBits + workerIdBits;
    // 时间戳移位
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    // 序列号最大值
    private long sequenceMask = -1L ^ (-1L << sequenceBits);
    // 上次时间戳
    private long lastTimestamp = -1L;

    // 构造函数,传入机器id和数据中心id
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        // 检查workerId和datacenterId是否在范围内
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // 产生下一个id
    public synchronized long nextId() {
        // 获取时间戳
        long timestamp = timeGen();
        // 如果时间戳小于上次时间戳,抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        // 如果时间戳与上次时间戳相同,序列号加1
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 如果序列号等于0,获取新的时间戳
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 重新计数
            sequence = 0L;
        }

        // 更新最后时间戳
        lastTimestamp = timestamp;
        // 移位并组合得到id
        long id = ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
        return id;
    }

    // 等待下一毫秒的时间戳
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    // 获取时间戳,单位毫秒
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
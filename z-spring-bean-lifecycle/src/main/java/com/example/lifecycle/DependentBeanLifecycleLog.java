package com.example.lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DependentBeanLifecycleLog {

    private static final List<String> orderLogs = Collections.synchronizedList(new ArrayList<>());
    private static final Map<Class<?>, AtomicInteger> seqPerClass = new ConcurrentHashMap<>();

    public static void log(Class<?> clz, String beanName, String event) {
        String className = clz.getSimpleName();
        int sequenceNumber = seqPerClass.computeIfAbsent(clz, k -> new AtomicInteger(0)).incrementAndGet();
        String logEntry = className + "[" + beanName + "]: " + sequenceNumber + ": " + event;
        System.out.println(logEntry);
        orderLogs.add(logEntry);
    }

    public static void clear() {
        orderLogs.clear();
        seqPerClass.clear();
    }

    public static List<String> getOrderLogs() {
        return Collections.unmodifiableList(orderLogs);
    }

    public static boolean contains(String pattern) {
        return orderLogs.stream().anyMatch(log -> log.contains(pattern));
    }

    public static List<String> getLogsForBean(String beanName) {
        return orderLogs.stream()
                .filter(log -> log.contains("[" + beanName + "]"))
                .toList();
    }
}

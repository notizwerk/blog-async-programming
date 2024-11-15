package de.notizwerk.async.chapter1_1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Analyzes thread memory consumption using Native Memory Tracking.
 * Run with JVM argument: -XX:NativeMemoryTracking=summary
 */
public class ThreadAnalyzer {
    private static final int THREAD_GROUP_SIZE = 100;
    private static final int NUM_GROUPS = 5;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Native Memory Tracking Analysis ===\n");
        printNMTStatus("Initial Status");

        List<Thread> allThreads = new ArrayList<>();

        // Create and analyze thread groups
        for (int group = 1; group <= NUM_GROUPS; group++) {
            System.out.printf("%n=== Thread Group %d (adding %d threads) ===%n",
                    group, THREAD_GROUP_SIZE);

            long[] beforeMem = getThreadMemory();
            List<Thread> newThreads = createThreads(THREAD_GROUP_SIZE);
            allThreads.addAll(newThreads);

            // Wait for GC to settle
            Thread.sleep(1000);
            System.gc();
            Thread.sleep(100);

            long[] afterMem = getThreadMemory();
            printMemoryDelta(beforeMem, afterMem, THREAD_GROUP_SIZE);
            printNMTStatus("After Group " + group);
        }

        // Cleanup
        allThreads.forEach(Thread::interrupt);
        for (Thread t : allThreads) {
            t.join(100);
        }
    }

    private static List<Thread> createThreads(int count) {
        List<Thread> threads = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            Thread t = new Thread(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "WorkerThread-" + i);
            t.start();
            threads.add(t);
        }

        try {
            readyLatch.await();
            startLatch.countDown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return threads;
    }

    private static void printNMTStatus(String phase) {
        try {
            Process p = new ProcessBuilder("jcmd",
                    String.valueOf(ProcessHandle.current().pid()),
                    "VM.native_memory",
                    "summary")
                    .redirectErrorStream(true)
                    .start();

            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor();

            System.out.println("\n=== " + phase + " ===");
            output.lines()
                    .filter(l -> l.contains("Total:") || l.contains("Thread"))
                    .forEach(System.out::println);

        } catch (Exception e) {
            System.err.println("Error getting NMT status: " + e);
        }
    }

    private static long[] getThreadMemory() throws Exception {
        Process p = new ProcessBuilder("jcmd",
                String.valueOf(ProcessHandle.current().pid()),
                "VM.native_memory",
                "summary")
                .redirectErrorStream(true)
                .start();

        String output = new String(p.getInputStream().readAllBytes());
        p.waitFor();

        long[] memory = new long[2]; // [0] = reserved, [1] = committed
        output.lines()
                .filter(l -> l.contains("Thread (reserved="))
                .findFirst()
                .ifPresent(l -> {
                    memory[0] = extractKB(l, "reserved=");
                    memory[1] = extractKB(l, "committed=");
                });

        return memory;
    }

    private static long extractKB(String line, String marker) {
        int start = line.indexOf(marker) + marker.length();
        int end = line.indexOf("KB", start);
        return Long.parseLong(line.substring(start, end).trim());
    }

    private static void printMemoryDelta(long[] before, long[] after, int threadCount) {
        long reservedDelta = after[0] - before[0];
        long committedDelta = after[1] - before[1];

        System.out.printf("Memory change for %d threads:%n", threadCount);
        System.out.printf("Reserved:  %6d KB total, %6.1f KB per thread%n",
                reservedDelta, (float)reservedDelta / threadCount);
        System.out.printf("Committed: %6d KB total, %6.1f KB per thread%n",
                committedDelta, (float)committedDelta / threadCount);
    }
}
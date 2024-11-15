package de.notizwerk.async.chapter1_1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstriert die Auswirkungen von Context Switching auf die Performance in Java.
 * Teil von Kapitel 1.1: "Die Herausforderung der Skalierung"
 */
public class ContextSwitchDemo {
    // Konstanten für bessere Lesbarkeit und einfache Anpassung
    private static final long TOTAL_ITERATIONS = 100_000_000L;
    private static final int THREAD_MULTIPLIER = 2;

    public static void main(String[] args) {
        // Test mit einem Thread
        long singleThreadTime = measureSingleThread();

        // Test mit vielen Threads
        long multiThreadTime = measureMultiThread();

        // Ergebnisse ausgeben
        System.out.printf("""
            Berechnungszeit Vergleich:
            Single Thread: %d ms
            Multi Thread:  %d ms
            Overhead:      %.2fx%n""",
                singleThreadTime, multiThreadTime,
                (double) multiThreadTime / singleThreadTime);
    }

    private static long measureSingleThread() {
        long start = System.nanoTime();

        // Eine CPU-intensive Berechnung in einem Thread
        long sum = 0;
        for (long i = 0; i < TOTAL_ITERATIONS; i++) {
            sum += i;
        }

        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }

    private static long measureMultiThread() {
        // Threads basierend auf CPU-Kernen erstellen
        int threadCount = Runtime.getRuntime().availableProcessors() * THREAD_MULTIPLIER;
        long iterationsPerThread = TOTAL_ITERATIONS / threadCount;

        CountDownLatch latch = new CountDownLatch(threadCount);
        long start = System.nanoTime();

        // Gleiche Berechnung auf mehrere Threads aufteilen
        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                long sum = 0;
                for (long i = 0; i < iterationsPerThread; i++) {
                    sum += i;
                }
                latch.countDown();
            }, "Calculator-" + t).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }
}
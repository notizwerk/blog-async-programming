package de.notizwerk.async.chapter1_2;

import org.openjdk.jmh.annotations.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * JMH Benchmark zum Vergleich verschiedener I/O-Ansätze in Java:
 * - Sequentielles blockierendes I/O
 * - Paralleles blockierendes I/O mit Thread-Pool
 * - Nicht-blockierendes I/O mittels NIO-Channels
 * - Asynchrones I/O mit AsynchronousFileChannel
 * ---
 * Der Benchmark simuliert reale I/O-Operationen durch:
 * - Erstellung mehrerer kleiner Dateien (je 1 KB)
 * - Hinzufügen künstlicher Latenz zur Simulation von Netzwerk-I/O
 * - Lesen der Dateien mit verschiedenen I/O-Strategien
 * - Messung des Durchsatzes (Operationen pro Sekunde)
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
public class IOBenchmark {
//    private static final int BUFFER_SIZE = 8192;
    private static final int FILE_SIZE = 1024;     // 1KB pro Datei
    private static final int FILE_COUNT = 100;      // Anzahl der Testdateien
    private static final long SIMULATED_LATENCY_MS = 2;  // Künstliche Latenz

    private List<Path> testFiles;
    private Path tempDir;
    private ExecutorService executor;

    /**
     * Setup-Methode zur Erstellung von Testdateien und Initialisierung des Thread-Pools.
     * Erstellt FILE_COUNT Dateien mit je FILE_SIZE Bytes zufälligem Inhalt.
     */
    @Setup
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("iobenchmark");
        testFiles = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        byte[] data = new byte[FILE_SIZE];

        // Testdateien mit Zufallsinhalt erstellen
        for (int i = 0; i < FILE_COUNT; i++) {
            Path file = tempDir.resolve("test" + i + ".dat");
            random.nextBytes(data);
            Files.write(file, data);
            testFiles.add(file);
        }

        executor = Executors.newFixedThreadPool(20);
        System.gc();
    }

    /**
     * "Cleanup"-Methode zum Entfernen der Testdateien und Beenden des Thread-Pools.
     */
    @TearDown
    public void tearDown() throws IOException {
        executor.shutdown();
        for (Path file : testFiles) {
            Files.deleteIfExists(file);
        }
        Files.deleteIfExists(tempDir);
    }

    /**
     * Simuliert I/O-Latenz durch Hinzufügen einer kurzen Verzögerung.
     * Macht den Benchmark realistischer durch Simulation von Netzwerk- oder Festplatten-Latenz.
     */
    private void simulateLatency() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Sequentielles blockierendes Lesen mit traditionellem java.io.
     * Liest Dateien nacheinander in einem einzelnen Thread.
     */
    @Benchmark
    public List<byte[]> blockingRead() throws IOException {
        List<byte[]> results = new ArrayList<>();
        for (Path file : testFiles) {
            byte[] content = Files.readAllBytes(file);
            simulateLatency();
            results.add(content);
        }
        return results;
    }

    /**
     * Paralleles blockierendes Lesen mittels Thread-Pool.
     * Jeder Datei-Lesevorgang läuft in einem eigenen Thread aus dem Pool.
     */
    @Benchmark
    public List<byte[]> parallelBlockingRead() throws ExecutionException, InterruptedException {
        List<Future<byte[]>> futures = new ArrayList<>();

        for (Path file : testFiles) {
            futures.add(executor.submit(() -> {
                byte[] content = Files.readAllBytes(file);
                simulateLatency();
                return content;
            }));
        }

        List<byte[]> results = new ArrayList<>();
        for (Future<byte[]> future : futures) {
            results.add(future.get());
        }
        return results;
    }

    /**
     * Nicht-blockierendes Lesen mittels NIO FileChannel.
     * Verwendet Java NIO's channel-basiertes I/O mit explizitem Buffer-Management.
     */
    @Benchmark
    public List<byte[]> nonBlockingRead() throws IOException, ExecutionException, InterruptedException {
        List<Future<byte[]>> futures = new ArrayList<>();

        for (Path file : testFiles) {
            futures.add(executor.submit(() -> {
                try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                    ByteBuffer buffer = ByteBuffer.allocate(FILE_SIZE);
                    channel.read(buffer);
                    simulateLatency();
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    return data;
                }
            }));
        }

        List<byte[]> results = new ArrayList<>();
        for (Future<byte[]> future : futures) {
            results.add(future.get());
        }
        return results;
    }

    /**
     * Asynchrones Lesen mittels AsynchronousFileChannel.
     * Demonstriert die asynchronen I/O-Fähigkeiten von Java NIO.2.
     */
    @Benchmark
    public List<byte[]> asyncFileRead() throws IOException, ExecutionException, InterruptedException {
        List<Future<byte[]>> futures = new ArrayList<>();

        for (Path file : testFiles) {
            futures.add(executor.submit(() -> {
                ByteBuffer buffer = ByteBuffer.allocate(FILE_SIZE);
                try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(
                        file, StandardOpenOption.READ)) {
                    Future<Integer> operation = channel.read(buffer, 0);
                    operation.get();
                    simulateLatency();
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    return data;
                }
            }));
        }

        List<byte[]> results = new ArrayList<>();
        for (Future<byte[]> future : futures) {
            results.add(future.get());
        }
        return results;
    }
}
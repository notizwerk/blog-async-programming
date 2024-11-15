# Moderne Asynchrone Programmierung in Java und Kotlin

Begleitendes Code-Repository zur Blogserie über asynchrone Programmierung in Java und Kotlin.

## Projektstruktur

```
src/
├── main/java/de/notizwerk/async/
│   ├── chapter1_1/                  # Skalierung und Threading
│   │   ├── ContextSwitchDemo.java
│   │   ├── SimpleHttpServer.java
│   │   └── ThreadAnalyzer.java
│   └── chapter1_2/                  # Blocking vs Non-Blocking I/O
│       └── BlockingDatabaseExample.java
└── jmh/java/de/notizwerk/async/
    └── chapter1_2/                  # Performance Benchmarks
        └── IOBenchmark.java
```

## Voraussetzungen

- Java 21
- Kotlin 1.9
- Gradle 8.11

## Setup

```bash
git clone https://github.com/yourusername/async-programming.git
cd async-programming
./gradlew build
```

## Benchmarks ausführen

```bash
./gradlew jmh
```
Ergebnisse werden in `build/results/jmh/results.json` gespeichert.

## Dependencies

- JMH 1.37
- H2 Database 2.2.224
- JUnit 5.10.0

## Lizenz

Apache 2.0

## Links

- [Blogserie](https://notizwerk.de/blog/async-programming)

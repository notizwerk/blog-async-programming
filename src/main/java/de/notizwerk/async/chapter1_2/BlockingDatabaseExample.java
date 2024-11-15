package de.notizwerk.async.chapter1_2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Repräsentiert einen Kunden mit ID, Name und E-Mail-Adresse.
 */
class Customer {
    private final long id;
    private final String name;
    private final String email;

    public Customer(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}

/**
 * Service-Klasse für Kundenverwaltung mit blockierendem Datenbankzugriff.
 * Demonstriert traditionelle JDBC-Verwendung mit Connection-Pool.
 */
class CustomerService {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public CustomerService(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Sucht einen Kunden anhand seiner ID in der Datenbank.
     * Blockierender Datenbankzugriff mittels JDBC.
     *
     * @param id Die ID des gesuchten Kunden
     * @return Der gefundene Kunde oder null, falls nicht vorhanden
     * @throws RuntimeException bei Datenbankfehlern
     */
    public Customer findCustomer(long id) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (var stmt = conn.prepareStatement(
                    "SELECT * FROM customers WHERE id = ?")) {
                stmt.setLong(1, id);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Customer(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("email")
                        );
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * Demonstriert die Verwendung von blockierendem JDBC für Datenbankzugriffe.
 * Nutzt eine H2-In-Memory-Datenbank für einfaches Testen.
 */
public class BlockingDatabaseExample {
    public static void main(String[] args) throws SQLException {
        // H2 In-Memory Datenbank Setup
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        String username = "sa";
        String password = "";

        // Datenbank initialisieren
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Kundentabelle anlegen
            conn.createStatement().execute("""
                CREATE TABLE customers (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255),
                    email VARCHAR(255)
                )""");

            // Beispielkunden einfügen
            try (var stmt = conn.prepareStatement(
                    "INSERT INTO customers (id, name, email) VALUES (?, ?, ?)")) {
                stmt.setLong(1, 1L);
                stmt.setString(2, "John Doe");
                stmt.setString(3, "john@example.com");
                stmt.executeUpdate();
            }
        }

        // Kundensuche testen
        CustomerService service = new CustomerService(jdbcUrl, username, password);
        Customer customer = service.findCustomer(1);
        System.out.println("Found customer: " + customer);
    }
}
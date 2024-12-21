package com.example.se302;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DATABASE_URL = "jdbc:sqlite:database/TimetableManagement.db";

    public static Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            System.out.println("SQLite veritabanına bağlanıldı.");
        } catch (SQLException e) {
            System.out.println("Bağlantı hatası: " + e.getMessage());
        }
        return connection;
    }

    public static void setupCourseAssignmentsTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS classroom_assignment (
                course_name TEXT NOT NULL,
                classroom_name TEXT NOT NULL,
                time_to_start TEXT NOT NULL,
                PRIMARY KEY (course_name, time_to_start)
            );
        """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


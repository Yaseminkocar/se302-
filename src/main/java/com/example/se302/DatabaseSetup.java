package com.example.se302;

import java.sql.*;

public class DatabaseSetup {

    private static final String DB_PATH = "jdbc:sqlite:database/TimetableManagement.db";

    public static void setupDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement()) {

            // Foreign key özelliğini etkinleştir
            statement.execute("PRAGMA foreign_keys = ON;");

            // courses tablosu oluştur
            String createCoursesTable = """
                CREATE TABLE IF NOT EXISTS courses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_name TEXT NOT NULL,
                    time_to_start TEXT NOT NULL,
                    duration INTEGER NOT NULL,
                    lecturer TEXT NOT NULL,
                    UNIQUE(course_name, time_to_start)
                );
                """;
            statement.execute(createCoursesTable);

            // students tablosu oluştur
            String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_name TEXT NOT NULL
                );
                """;
            statement.execute(createStudentsTable);

            // course_students tablosu oluştur
            String createCourseStudentsTable = """
                CREATE TABLE IF NOT EXISTS course_students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_id INTEGER NOT NULL,
                    student_id INTEGER NOT NULL,
                    UNIQUE(course_id, student_id),
                    FOREIGN KEY (course_id) REFERENCES courses (id),
                    FOREIGN KEY (student_id) REFERENCES students (id)
                );
                """;
            statement.execute(createCourseStudentsTable);

            System.out.println("Database and tables created successfully.");

        } catch (SQLException e) {
            System.err.println("SQL error during database setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void checkDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement()) {

            String checkTablesQuery = """
                SELECT name FROM sqlite_master
                WHERE type='table' AND name IN ('courses', 'students', 'course_students');
            """;
            ResultSet resultSet = statement.executeQuery(checkTablesQuery);

            if (!resultSet.next()) {
                System.out.println("One or more tables are missing. Running setupDatabase...");
                setupDatabase();
            } else {
                System.out.println("All tables are present.");
            }

        } catch (SQLException e) {
            System.err.println("SQL error during database check: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

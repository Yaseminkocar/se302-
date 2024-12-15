package com.example.se302;

import java.sql.*;

public class DatabaseSetup {

    private static final String DB_PATH = "jdbc:sqlite:C:\\database\\TimetableManagement.db";



    public static void setupDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement()) {

            // courses tablosu oluştur
            String createCoursesTable = """
                CREATE TABLE IF NOT EXISTS courses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_name TEXT NOT NULL,
                    time_to_start TEXT NOT NULL,
                    duration INTEGER NOT NULL,
                    lecturer TEXT NOT NULL
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
                    FOREIGN KEY (course_id) REFERENCES courses (id),
                    FOREIGN KEY (student_id) REFERENCES students (id)
                );
                """;
            statement.execute(createCourseStudentsTable);

            System.out.println("Database and tables created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void checkDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement()) {

            // Eğer tablo zaten varsa hiçbir şey yapma
            String checkTableQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='courses';";
            ResultSet resultSet = statement.executeQuery(checkTableQuery);

            if (!resultSet.next()) {
                // Tablo yoksa oluştur
                String createCoursesTable = "CREATE TABLE courses (course_name TEXT, time_to_start TEXT, duration INTEGER, lecturer TEXT)";
                statement.executeUpdate(createCoursesTable);
                System.out.println("Courses table created.");
            } else {
                System.out.println("Courses table already exists.");
            }

        } catch (Exception e) {
            e.printStackTrace();
}
}
}
package com.example.se302;
import java.io.*;
import java.sql.*;

public class SecondDatabase {

    private static final String CLASSROOM_DB_PATH = "jdbc:sqlite:database/ClassroomCapacity.db";
    // Remove hardcoded CSV path since it will be passed as parameter


    public static void main(String[] args) {
        createDatabaseDirectory(); // Klasörü oluştur
        // Remove direct CSV_FILE_PATH reference - path should be provided as argument
        if (args.length > 0) {
            importClassroomCapacity(args[0]);
        } else {
            System.err.println("Please provide the CSV file path as an argument");
        }
    }

    public static void createDatabaseDirectory() {
        File directory = new File("database");
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Database directory created at: " + directory.getPath());
        }
        else {
            System.out.println("Database directory already exists: ");
        }
    }

    public static void importClassroomCapacity(String filePath) {
        // Add file existence check
        File csvFile = new File(filePath);
        if (!csvFile.exists()) {
            System.err.println("Error: CSV file not found at: " + filePath);
            return;
        }

        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS classroom_capacity (
                Classroom TEXT PRIMARY KEY,
                Capacity INTEGER
            );
            """;

        String insertSQL = "INSERT OR IGNORE INTO classroom_capacity (Classroom, Capacity) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(CLASSROOM_DB_PATH)) {
            // Tablo oluştur
            try (PreparedStatement createTableStmt = connection.prepareStatement(createTableSQL)) {
                createTableStmt.execute();
                System.out.println("Table 'classroom_capacity' created successfully.");
            }

            // CSV dosyasını oku ve verileri ekle
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
                 PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {

                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false; // Başlık satırını atla
                        continue;
                    }

                    String[] columns = line.split(";");
                    if (columns.length >= 2) {
                        String classroom = columns[0].trim();
                        int capacity = Integer.parseInt(columns[1].trim());

                        System.out.println("Inserting Classroom: " + classroom + ", Capacity: " + capacity);
                        insertStmt.setString(1, classroom);
                        insertStmt.setInt(2, capacity);
                        insertStmt.addBatch();
                    }
                }

                insertStmt.executeBatch();
                System.out.println("Classroom capacities imported successfully into ClassroomCapacity.db.");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
package com.example.se302;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.example.se302.DatabaseHelper.courseExists;

public class CSVToDatabase {

    private static final String DB_PATH = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\TimetableManagement.db";


    public static void importCSV(String filePath) {
        String insertSQL = "INSERT OR IGNORE INTO courses (course_name, time_to_start, duration, lecturer) VALUES (?, ?, ?, ?)";

      /*  try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isFirstLine = true;
            int batchSize = 100;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Başlık satırını atla
                    continue;
                }

                String[] columns = line.split(";");
                if (columns.length >= 4) {
                    try {
                        String courseName = columns[0].trim();
                        String timeToStart = columns[1].trim();
                        int duration = Integer.parseInt(columns[2].trim());
                        String lecturer = columns[3].trim();

                        preparedStatement.setString(1, courseName);
                        preparedStatement.setString(2, timeToStart);
                        preparedStatement.setInt(3, duration);
                        preparedStatement.setString(4, lecturer);

                        preparedStatement.addBatch();
                        count++;

                        if (count % batchSize == 0) {
                            preparedStatement.executeBatch();
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format in row: " + line);
                    }
                }
            }
            preparedStatement.executeBatch(); // Kalan işlemleri tamamla
            System.out.println("Data imported successfully.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } */

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Başlık satırını atla
                }

                String[] columns = line.split(";");
                if (columns.length >= 4) {
                    String courseName = columns[0].trim();
                    String timeToStart = columns[1].trim();
                    int duration = Integer.parseInt(columns[2].trim());
                    String lecturer = columns[3].trim();

                    // Kaydın varlığını kontrol et
                    if (!courseExists(courseName, timeToStart)) {
                        preparedStatement.setString(1, courseName);
                        preparedStatement.setString(2, timeToStart);
                        preparedStatement.setInt(3, duration);
                        preparedStatement.setString(4, lecturer);

                        preparedStatement.addBatch();
                    }
                }
            }

            preparedStatement.executeBatch(); // Tüm kayıtları bir seferde ekle
            System.out.println("Data imported successfully.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}

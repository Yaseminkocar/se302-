package com.example.se302;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.example.se302.DatabaseHelper.courseExists;

public class CSVToDatabase {

    private static final String DB_PATH = "jdbc:sqlite:C:\\database\\TimetableManagement.db";
    private static final String CSV_FILE_PATH = "C:/database/ClassroomCapacity.csv";

    private static void createDatabaseTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS classroom_capacity (
                Classroom TEXT PRIMARY KEY,
                Capacity INTEGER
            );
            """;

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement statement = connection.prepareStatement(createTableSQL)) {

            statement.execute();
            System.out.println("Table 'classroom_capacity' created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


   /* public static void importCSV(String filePath) {
        String insertSQL = "INSERT OR IGNORE INTO courses (course_name, time_to_start, duration, lecturer) VALUES (?, ?, ?, ?)";

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


    }*/

    public static void importCSV(String filePath) {
        HashSet<String> existingStudents = new HashSet<>();
        List<String[]> coursesAndStudents = readCSVFile(filePath);

        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            // Ders ekleme
            String insertCourseSQL = "INSERT OR IGNORE INTO courses (course_name, time_to_start, duration, lecturer) VALUES (?, ?, ?, ?)";
            PreparedStatement insertCourseStmt = connection.prepareStatement(insertCourseSQL);

            // Öğrenci ekleme
            String insertStudentSQL = "INSERT OR IGNORE INTO students (student_name) VALUES (?)";
            PreparedStatement insertStudentStmt = connection.prepareStatement(insertStudentSQL);

            // Ders-Öğrenci ilişkilendirme
            String selectStudentIdSQL = "SELECT id FROM students WHERE student_name = ?";
            String insertCourseStudentSQL = "INSERT INTO course_students (course_id, student_id) VALUES (?, ?)";
            PreparedStatement selectStudentIdStmt = connection.prepareStatement(selectStudentIdSQL);
            PreparedStatement insertCourseStudentStmt = connection.prepareStatement(insertCourseStudentSQL);

            for (String[] record : coursesAndStudents) {
                String courseName = record[0];
                String timeToStart = record[1];
                int duration = Integer.parseInt(record[2]);
                String lecturer = record[3];
                String studentName = record[4];

                // 1. Ders ekle
                insertCourseStmt.setString(1, courseName);
                insertCourseStmt.setString(2, timeToStart);
                insertCourseStmt.setInt(3, duration);
                insertCourseStmt.setString(4, lecturer);
                insertCourseStmt.addBatch();

                // 2. Öğrenciyi ekle
                if (!existingStudents.contains(studentName)) {
                    insertStudentStmt.setString(1, studentName);
                    insertStudentStmt.executeUpdate();
                    existingStudents.add(studentName);
                }

                // 3. Öğrenci ID'sini al
                int studentId = -1;
                selectStudentIdStmt.setString(1, studentName);
                try (ResultSet rs = selectStudentIdStmt.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("id");
                    }
                }

                // 4. Ders-Öğrenci ilişkilendir
                if (studentId != -1) {
                    int courseId = getCourseId(connection, courseName);
                    if (courseId != -1) {
                        insertCourseStudentStmt.setInt(1, courseId);
                        insertCourseStudentStmt.setInt(2, studentId);
                        insertCourseStudentStmt.executeUpdate();
                    }
                }
            }

            insertCourseStmt.executeBatch(); // Tüm dersleri ekle
            System.out.println("Data imported successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }





    private static List<String[]> readCSVFile(String filePath) {
        List<String[]> coursesAndStudents = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Başlık satırını atla
                }

                String[] columns = line.split(";");
                if (columns.length >= 5) { // En az 5 sütun kontrolü
                    String courseName = columns[0].trim();
                    String timeToStart = columns[1].trim();
                    String duration = columns[2].trim();
                    String lecturer = columns[3].trim();

                    // Öğrencileri sütunlardan ayır
                    for (int i = 4; i < columns.length; i++) {
                        String studentName = columns[i].trim();
                        if (!studentName.isEmpty()) {
                            coursesAndStudents.add(new String[]{courseName, timeToStart, duration, lecturer, studentName});
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return coursesAndStudents;
    }

    private static int getCourseId(Connection connection, String courseName) throws SQLException {
        String selectCourseIdSQL = "SELECT id FROM courses WHERE course_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectCourseIdSQL)) {
            stmt.setString(1, courseName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1; // Bulunamazsa -1 döner
}

    public static void importClassroomCapacity(String filePath) {
        // Tablo oluşturma sorgusu
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS classroom_capacity (
                Classroom TEXT PRIMARY KEY,
                Capacity INTEGER
            );
            """;

        // Veri ekleme sorgusu
        String insertSQL = "INSERT OR IGNORE INTO classroom_capacity (Classroom, Capacity) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(CSV_FILE_PATH )) {
            // Veritabanı oluştur ve tabloyu hazırla
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

                    // CSV'den veriyi oku
                    String[] columns = line.split(";");
                    if (columns.length >= 2) {
                        String classroom = columns[0].trim();
                        int capacity = Integer.parseInt(columns[1].trim());

                        // Veriyi tabloya ekle
                        insertStmt.setString(1, classroom);
                        insertStmt.setInt(2, capacity);
                        insertStmt.addBatch();
                    }
                }

                // Tüm verileri ekle
                insertStmt.executeBatch();
                System.out.println("Classroom capacities imported successfully into ClassroomCapacity.db.");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}
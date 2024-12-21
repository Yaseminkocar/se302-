package com.example.se302;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class CSVToDatabase {

    private static final String DB_PATH = "jdbc:sqlite:database/TimetableManagement.db";
    //private static final String CLASSROOM_DB_PATH = "jdbc:sqlite:database/ClassroomCapacity.db";

    public static void createDatabaseTables() {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            // Create courses table
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

            // Create students table
            String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_name TEXT NOT NULL UNIQUE
                );
                """;

            // Create course_students table
            String createCourseStudentsTable = """
                CREATE TABLE IF NOT EXISTS course_students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_id INTEGER NOT NULL,
                    student_id INTEGER NOT NULL,
                    FOREIGN KEY (course_id) REFERENCES courses (id),
                    FOREIGN KEY (student_id) REFERENCES students (id),
                    UNIQUE(course_id, student_id)
                );
                """;

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createCoursesTable);
                stmt.execute(createStudentsTable);
                stmt.execute(createCourseStudentsTable);

                System.out.println("All tables created successfully.");
            }

        } catch (SQLException e) {
            System.err.println("Error creating database tables: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /*public static void importCSV(String filePath) {
        HashSet<String> existingStudents = new HashSet<>();
        List<String[]> coursesAndStudents = readCSVFile(filePath, ";");

        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            Map<String, Integer> coursesMap = getAllCourses(connection);

            String insertCourseSQL = "INSERT OR IGNORE INTO courses (course_name, time_to_start, duration, lecturer) VALUES (?, ?, ?, ?)";
            String insertStudentSQL = "INSERT OR IGNORE INTO students (student_name) VALUES (?)";
            String insertCourseStudentSQL = "INSERT INTO course_students (course_id, student_id) VALUES (?, ?)";

            try (PreparedStatement insertCourseStmt = connection.prepareStatement(insertCourseSQL);
                 PreparedStatement insertStudentStmt = connection.prepareStatement(insertStudentSQL);
                 PreparedStatement insertCourseStudentStmt = connection.prepareStatement(insertCourseStudentSQL)) {

                for (String[] record : coursesAndStudents) {
                    String courseName = record[0];
                    String timeToStart = record[1];
                    int duration = Integer.parseInt(record[2]);
                    String lecturer = record[3];
                    String studentName = record[4];

                    // Insert course
                    insertCourseStmt.setString(1, courseName);
                    insertCourseStmt.setString(2, timeToStart);
                    insertCourseStmt.setInt(3, duration);
                    insertCourseStmt.setString(4, lecturer);
                    insertCourseStmt.addBatch();

                    // Insert student
                    if (!existingStudents.contains(studentName)) {
                        insertStudentStmt.setString(1, studentName);
                        insertStudentStmt.executeUpdate();
                        existingStudents.add(studentName);
                    }

                    // Insert course-student relationship
                    int courseId = coursesMap.getOrDefault(courseName, -1);
                    if (courseId != -1) {
                        int studentId = getStudentId(connection, studentName);
                        if (studentId != -1) {
                            insertCourseStudentStmt.setInt(1, courseId);
                            insertCourseStudentStmt.setInt(2, studentId);
                            insertCourseStudentStmt.executeUpdate();
                        }
                    }
                }

                insertCourseStmt.executeBatch();
                System.out.println("Data imported successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } */

    public static void importCSV(String filePath) {
        HashSet<String> existingStudents = new HashSet<>();
        List<String[]> coursesAndStudents = readCSVFile(filePath, ";");

        try (Connection connection = DriverManager.getConnection(DB_PATH)) {

            String insertCourseSQL = "INSERT OR IGNORE INTO courses (course_name, time_to_start, duration, lecturer) VALUES (?, ?, ?, ?)";
            String insertStudentSQL = "INSERT OR IGNORE INTO students (student_name) VALUES (?)";
            String insertCourseStudentSQL = "INSERT INTO course_students (course_id, student_id) VALUES (?, ?)";
            String getCourseIdSQL = "SELECT id FROM courses WHERE course_name = ? AND time_to_start = ?";
            String getStudentIdSQL = "SELECT id FROM students WHERE student_name = ?";
            String checkCourseStudentSQL = "SELECT 1 FROM course_students WHERE course_id = ? AND student_id = ?";

            try (PreparedStatement insertCourseStmt = connection.prepareStatement(insertCourseSQL);
                 PreparedStatement insertStudentStmt = connection.prepareStatement(insertStudentSQL);
                 PreparedStatement insertCourseStudentStmt = connection.prepareStatement(insertCourseStudentSQL);
                 PreparedStatement getCourseIdStmt = connection.prepareStatement(getCourseIdSQL);
                 PreparedStatement getStudentIdStmt = connection.prepareStatement(getStudentIdSQL);
                 PreparedStatement checkCourseStudentStmt = connection.prepareStatement(checkCourseStudentSQL)) {

                for (String[] record : coursesAndStudents) {
                    String courseName = record[0];
                    String timeToStart = record[1];
                    int duration = Integer.parseInt(record[2]);
                    String lecturer = record[3];
                    String studentName = record[4];

                    // Insert course
                    insertCourseStmt.setString(1, courseName);
                    insertCourseStmt.setString(2, timeToStart);
                    insertCourseStmt.setInt(3, duration);
                    insertCourseStmt.setString(4, lecturer);
                    insertCourseStmt.executeUpdate();

                    // Insert student
                    if (!existingStudents.contains(studentName)) {
                        insertStudentStmt.setString(1, studentName);
                        insertStudentStmt.executeUpdate();
                        existingStudents.add(studentName);
                    }

                    // Get course_id
                    int courseId = -1;
                    getCourseIdStmt.setString(1, courseName);
                    getCourseIdStmt.setString(2, timeToStart);
                    try (ResultSet courseResult = getCourseIdStmt.executeQuery()) {
                        if (courseResult.next()) {
                            courseId = courseResult.getInt("id");
                        }
                    }

                    // Get student_id
                    int studentId = -1;
                    getStudentIdStmt.setString(1, studentName);
                    try (ResultSet studentResult = getStudentIdStmt.executeQuery()) {
                        if (studentResult.next()) {
                            studentId = studentResult.getInt("id");
                        }
                    }

                    if (courseId != -1 && studentId != -1) {
                        // Check for duplicate course-student relationship
                        checkCourseStudentStmt.setInt(1, courseId);
                        checkCourseStudentStmt.setInt(2, studentId);
                        ResultSet checkResult = checkCourseStudentStmt.executeQuery();

                        if (!checkResult.next()) {
                            insertCourseStudentStmt.setInt(1, courseId);
                            insertCourseStudentStmt.setInt(2, studentId);
                            insertCourseStudentStmt.executeUpdate();
                        } else {
                            System.out.println("Duplicate entry skipped: course_id=" + courseId + ", student_id=" + studentId);
                        }
                    } else {
                        System.out.println("Invalid course or student: course=" + courseName + ", student=" + studentName);
                    }
                }

                System.out.println("Data imported successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private static List<String[]> readCSVFile(String filePath, String delimiter) {
        List<String[]> coursesAndStudents = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = line.split(delimiter);
                if (columns.length >= 5) {
                    String courseName = columns[0].trim();
                    String timeToStart = columns[1].trim();
                    String duration = columns[2].trim();
                    String lecturer = columns[3].trim();

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

    private static Map<String, Integer> getAllCourses(Connection connection) throws SQLException {
        Map<String, Integer> coursesMap = new HashMap<>();
        String query = "SELECT id, course_name FROM courses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                coursesMap.put(rs.getString("course_name"), rs.getInt("id"));
            }
        }
        return coursesMap;
    }

    private static int getStudentId(Connection connection, String studentName) throws SQLException {
        String selectStudentIdSQL = "SELECT id FROM students WHERE student_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectStudentIdSQL)) {
            stmt.setString(1, studentName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    public static void importClassroomCapacity(String filePath) {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS classroom_capacity (
                Classroom TEXT PRIMARY KEY,
                Capacity INTEGER
            );
            """;

        String insertSQL = "INSERT OR IGNORE INTO classroom_capacity (Classroom, Capacity) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement createTableStmt = connection.prepareStatement(createTableSQL);
             PreparedStatement insertStmt = connection.prepareStatement(insertSQL);
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            createTableStmt.execute();
            System.out.println("Table 'classroom_capacity' created successfully.");

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header row
                    continue;
                }

                String[] columns = line.split(";");
                if (columns.length >= 2) {
                    try {
                        String classroom = columns[0].trim();
                        int capacity = Integer.parseInt(columns[1].trim());

                        insertStmt.setString(1, classroom);
                        insertStmt.setInt(2, capacity);
                        insertStmt.addBatch();
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid capacity value: " + columns[1]);
                    }
                }
            }

            insertStmt.executeBatch();
            System.out.println("Classroom capacities imported successfully.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}

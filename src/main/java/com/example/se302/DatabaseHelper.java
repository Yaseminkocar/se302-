package com.example.se302;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {

    private static final String DB_PATH = "jdbc:sqlite:C:\\database\\TimetableManagement.db";

    static {
        // Ensure the database file exists
        File dbFile = new File(System.getProperty("user.dir") + "\\TimetableManagement.db");

        try {
            if (!dbFile.exists()) {
                System.out.println("database have not found , creating...");
                dbFile.createNewFile(); // Create the file if it does not exist
                setupDatabase(); // Create the database and tables
            } else {
                System.out.println("already have database.");
            }
        } catch (IOException e) {
            System.err.println("there was a mistake when creating a database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static boolean courseExists(String courseName, String timeToStart) {
        String query = "SELECT 1 FROM courses WHERE course_name = ? AND time_to_start = ?";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, courseName);
            preparedStatement.setString(2, timeToStart);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // Eğer sonuç dönerse kayıt var demektir.

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Setup the database
    private static void setupDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement()) {

            // Create courses table
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

            // Create students table
            String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_name TEXT NOT NULL
                );
                """;
            statement.execute(createStudentsTable);

            // Create course_students table
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

    public static void removeDuplicates() {
        String deleteSQL = """
        DELETE FROM courses
        WHERE id NOT IN (
            SELECT MIN(id)
            FROM courses
            GROUP BY course_name, time_to_start
        );
        """;

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(deleteSQL);
            System.out.println("Duplicate records removed.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Import data from CSV to SQLite
    public static void importCSV(String filePath) {
        String insertCourseSQL = "INSERT INTO courses (course_name, time_to_start, duration, lecturer) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(insertCourseSQL);
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(";");
                if (columns.length >= 4) {
                    String courseName = columns[0].trim();
                    String timeToStart = columns[1].trim();
                    int duration = Integer.parseInt(columns[2].trim());
                    String lecturer = columns[3].trim();

                    preparedStatement.setString(1, courseName);
                    preparedStatement.setString(2, timeToStart);
                    preparedStatement.setInt(3, duration);
                    preparedStatement.setString(4, lecturer);

                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();
            System.out.println("Data imported successfully from CSV.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    // Tüm dersleri al
    public static List<String> getCourses() {
        List<String> courses = new ArrayList<>();
        String query = "SELECT course_name FROM courses";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                courses.add(resultSet.getString("course_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    // Bir derse kayıtlı öğrencileri al
    public static List<String> getStudentsByCourse(String courseName) {
        List<String> students = new ArrayList<>();
        String query = """
            SELECT students.student_name
            FROM students
            INNER JOIN course_students ON students.id = course_students.student_id
            INNER JOIN courses ON courses.id = course_students.course_id
            WHERE courses.course_name = ?;
            """;

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, courseName);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                students.add(resultSet.getString("student_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    // Yeni bir ders ekle
    public static void addCourse(String courseName, String timeToStart, int duration, String lecturer) {
        String insertSQL = "INSERT INTO courses (course_name, time_to_start, duration, lecturer) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, courseName);
            preparedStatement.setString(2, timeToStart);
            preparedStatement.setInt(3, duration);
            preparedStatement.setString(4, lecturer);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Yeni bir öğrenci ekle
    public static void addStudent(String studentName) {
        String insertSQL = "INSERT INTO students (student_name) VALUES (?)";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, studentName);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Bir öğrenci ve ders arasında ilişki oluştur
    public static void assignStudentToCourse(String courseName, String studentName) {
        String courseQuery = "SELECT id FROM courses WHERE course_name = ?";
        String studentQuery = "SELECT id FROM students WHERE student_name = ?";
        String insertSQL = "INSERT INTO course_students (course_id, student_id) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement courseStatement = connection.prepareStatement(courseQuery);
             PreparedStatement studentStatement = connection.prepareStatement(studentQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            // Ders ID'sini al
            courseStatement.setString(1, courseName);
            ResultSet courseResult = courseStatement.executeQuery();
            if (!courseResult.next()) {
                System.out.println("Course not found: " + courseName);
                return;
            }
            int courseId = courseResult.getInt("id");

            // Öğrenci ID'sini al
            studentStatement.setString(1, studentName);
            ResultSet studentResult = studentStatement.executeQuery();
            if (!studentResult.next()) {
                System.out.println("Student not found: " + studentName);
                return;
            }
            int studentId = studentResult.getInt("id");

            // İlişkiyi ekle
            insertStatement.setInt(1, courseId);
            insertStatement.setInt(2, studentId);
            insertStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static List<String> getClassroomsFromDatabase() {
        List<String> classrooms = new ArrayList<>();
        String query = "SELECT DISTINCT course_name FROM courses";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                classrooms.add(resultSet.getString("course_name"));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching classrooms: " + e.getMessage());
        }

        return classrooms;
    }

    public static ObservableList<CourseSchedule> fetchCourseData(String classroom) {
        ObservableList<CourseSchedule> data = FXCollections.observableArrayList();
        String query = "SELECT time_to_start AS time, course_name, lecturer FROM courses WHERE course_name = ?";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, classroom);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String time = resultSet.getString("time");
                String courseName = resultSet.getString("course_name");
                String lecturer = resultSet.getString("lecturer");

                data.add(new CourseSchedule(time, courseName, lecturer, "", "", ""));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }



    public static List<String> searchCoursesByLecturer(String lecturerName) {
        List<String> results = new ArrayList<>();
        String query = "SELECT DISTINCT course_name FROM courses WHERE lecturer LIKE ?";
        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + lecturerName + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString("course_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;


    }

    public static List<String> searchCoursesByStudent(String studentName) {
        List<String> results = new ArrayList<>();
        String query = """
        SELECT GROUP_CONCAT(DISTINCT courses.course_name) AS courses
        FROM course_students
        INNER JOIN students ON course_students.student_id = students.id
        INNER JOIN courses ON course_students.course_id = courses.id
        WHERE students.student_name = ?
        GROUP BY students.student_name;
    """;

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:C:\\database\\TimetableManagement.db");
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, studentName.trim()); // Kullanıcının girdiği isimle eşleştir
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String courses = resultSet.getString("courses");
                if (courses != null) {
                    results.add(courses);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }



    private static final String CLASSROOM_DB_PATH = "jdbc:sqlite:C:/database/ClassroomCapacity.db";

    // Classroom kapasitelerini getir
    public static List<String> getClassroomCapacities() {
        List<String> capacities = new ArrayList<>();
        String query = "SELECT Classroom, Capacity FROM classroom_capacity";

        try (Connection connection = DriverManager.getConnection(CLASSROOM_DB_PATH);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String classroom = resultSet.getString("Classroom");
                int capacity = resultSet.getInt("Capacity");
                capacities.add("Classroom: " + classroom + ", Capacity: " + capacity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return capacities;
    }

    // Belirli bir sınıfın kapasitesini getir
    public static int getClassroomCapacity(String classroomName) {
        String query = "SELECT Capacity FROM classroom_capacity WHERE Classroom = ?";
        try (Connection connection = DriverManager.getConnection(CLASSROOM_DB_PATH);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, classroomName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("Capacity");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Sınıf bulunamazsa -1 döner
}

    public static int getStudentCountForCourse(String courseName) {
        String query = """
        SELECT COUNT(DISTINCT students.id) AS student_count
        FROM course_students
        INNER JOIN courses ON course_students.course_id = courses.id
        INNER JOIN students ON course_students.student_id = students.id
        WHERE courses.course_name = ?;
    """;

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, courseName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("student_count");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0; // Eğer bir hata oluşursa veya sonuç yoksa 0 döner
    }

    public static List<String> getAvailableClassrooms(String courseName) {
        int studentCount = getStudentCountForCourse(courseName); // İlk veritabanından öğrenci sayısını al

        if (studentCount <= 0) {
            System.out.println("No students found for course: " + courseName);
            return new ArrayList<>();
        }

        String query = "SELECT Classroom, Capacity FROM classroom_capacity WHERE Capacity >= ?";

        List<String> availableClassrooms = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(CLASSROOM_DB_PATH);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, studentCount); // Kapasiteyi öğrenci sayısına göre filtrele
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String classroom = resultSet.getString("Classroom");
                int capacity = resultSet.getInt("Capacity");
                availableClassrooms.add("Classroom: " + classroom + ", Capacity: " + capacity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableClassrooms;
    }

    public static final String[] TIME_SLOTS = {
            "08:30 - 09:15",
            "09:25 - 10:10",
            "10:20 - 11:05",
            "11:15 - 12:00",
            "12:10 - 12:55",
            "13:05 - 13:50",
            "14:00 - 14:45",
            "14:55 - 15:40",
            "15:50 - 16:35"
    };

    public static Map<String, Map<String, String>> getWeeklyScheduleForStudentWithTimes(String studentName) {
        String query = """
        SELECT DISTINCT courses.course_name, courses.time_to_start
        FROM courses
        INNER JOIN course_students ON courses.id = course_students.course_id
        INNER JOIN students ON students.id = course_students.student_id
        WHERE students.student_name = ?
    """;

        Map<String, Map<String, String>> schedule = new LinkedHashMap<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        // Gün ve zaman dilimleri için boş program oluştur
        for (String day : days) {
            Map<String, String> timeMap = new LinkedHashMap<>();
            for (String time : TIME_SLOTS) {
                timeMap.put(time, "-"); // Başlangıçta her slot boş
            }
            schedule.put(day, timeMap);
        }

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, studentName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String courseName = resultSet.getString("course_name");
                String timeToStart = resultSet.getString("time_to_start");

                // Gün ve saat eşleştirmesi
                for (String day : days) {
                    if (timeToStart.toLowerCase().contains(day.toLowerCase())) {
                        for (String time : TIME_SLOTS) {
                            if (timeToStart.contains(time.split(" ")[0])) {
                                schedule.get(day).put(time, courseName);
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return schedule;
    }





}
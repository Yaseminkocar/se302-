package com.example.se302;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DatabaseHelper {

    private static final String DB_PATH = "jdbc:sqlite:database/TimetableManagement.db";

    static {
        // Ensure the database file exists
        File dbFile = new File("database/TimetableManagement.db");

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

    /*public static void removeDuplicates() {
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
    } */

    public static void removeDuplicateCourses() {
        String findDuplicatesQuery = """
        SELECT course_name, time_to_start, COUNT(*)
        FROM courses
        GROUP BY course_name, time_to_start
        HAVING COUNT(*) > 1
    """;

        String deleteDuplicatesQuery = """
        DELETE FROM courses
        WHERE id NOT IN (
            SELECT MIN(id)
            FROM courses
            GROUP BY course_name, time_to_start
        )
    """;

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement()) {

            // Çift kayıtları bul
            ResultSet resultSet = statement.executeQuery(findDuplicatesQuery);
            System.out.println("Duplicate Courses Found:");
            while (resultSet.next()) {
                String courseName = resultSet.getString("course_name");
                String timeToStart = resultSet.getString("time_to_start");
                int count = resultSet.getInt("COUNT(*)");
                System.out.println("Course: " + courseName + ", Time: " + timeToStart + ", Count: " + count);
            }

            // Çift kayıtları sil
            int rowsAffected = statement.executeUpdate(deleteDuplicatesQuery);
            System.out.println("Duplicate courses removed: " + rowsAffected);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeInvalidCourseStudents() {
        String deleteInvalidCourseStudentsQuery = """
        DELETE FROM course_students
        WHERE course_id NOT IN (SELECT id FROM courses)
    """;

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             Statement statement = connection.createStatement()) {

            // Geçersiz `course_id` referanslarını sil
            int rowsAffected = statement.executeUpdate(deleteInvalidCourseStudentsQuery);
            System.out.println("Invalid course-student relationships removed: " + rowsAffected);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void cleanDatabase() {
        System.out.println("Starting database cleaning...");

        // Çift kayıtları temizle
        removeDuplicateCourses();

        // Geçersiz referansları temizle
        removeInvalidCourseStudents();

        System.out.println("Database cleaning completed.");
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

        try (Connection connection = DriverManager.getConnection(DB_PATH);
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

    public static Map<String, Object> searchCourseDetails(String courseName) {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> studentList = new ArrayList<>();
        String courseQuery = """
        SELECT courses.lecturer, courses.duration, COUNT(DISTINCT course_students.student_id) AS student_count
        FROM courses
        LEFT JOIN course_students ON courses.id = course_students.course_id
        WHERE courses.course_name = ?
        GROUP BY courses.id, courses.lecturer, courses.duration;
    """;

        String studentQuery = """
        SELECT DISTINCT students.student_name
        FROM students
        INNER JOIN course_students ON students.id = course_students.student_id
        INNER JOIN courses ON courses.id = course_students.course_id
        WHERE courses.course_name = ?;
    """;


        try (Connection connection = DriverManager.getConnection(DB_PATH)) {

            // Ders detayları sorgusu
            try (PreparedStatement courseStmt = connection.prepareStatement(courseQuery)) {
                courseStmt.setString(1, courseName.trim());
                ResultSet courseResult = courseStmt.executeQuery();

                if (courseResult.next()) {
                    resultMap.put("Lecturer", courseResult.getString("lecturer"));
                    resultMap.put("Duration", courseResult.getInt("duration"));
                    resultMap.put("Student Count", courseResult.getInt("student_count"));
                }


                else {
                    resultMap.put("Message", "Course not found");
                    return resultMap; // Ders bulunamazsa hemen dön
                }
            }

            // Öğrenci listesi sorgusu
            try (PreparedStatement studentStmt = connection.prepareStatement(studentQuery)) {
                studentStmt.setString(1, courseName.trim());
                ResultSet studentResult = studentStmt.executeQuery();

                while (studentResult.next()) {
                    studentList.add(studentResult.getString("student_name"));
                }
            }

            resultMap.put("Student List", studentList); // Öğrenci listesi ekleniyor

        } catch (SQLException e) {
            e.printStackTrace();
            resultMap.put("Error", "Database error occurred: " + e.getMessage());
        }

        return resultMap;
    }

    private static final String CLASSROOM_DB_PATH = "jdbc:sqlite:database/ClassroomCapacity.db";



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

                // time_to_start'ı parse et
                String[] parts = timeToStart.split(" "); // Örn: ["Monday", "08:30"]
                if (parts.length == 2) {
                    String day = parts[0];
                    String time = parts[1];

                    // Gün ve saat eşleşmesi
                    for (String scheduledDay : days) {
                        if (day.equalsIgnoreCase(scheduledDay)) {
                            for (String slot : TIME_SLOTS) {
                                if (slot.contains(time)) { // Doğru zaman dilimini bul
                                    schedule.get(scheduledDay).put(slot, courseName);
                                    break;
                                }
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

    public static boolean checkAndAddStudentToCourse(String studentName, String courseName) {
        String conflictQuery = """
        SELECT 1
        FROM course_students cs
        INNER JOIN students s ON cs.student_id = s.id
        INNER JOIN courses c1 ON cs.course_id = c1.id
        INNER JOIN courses c2 ON c2.course_name = ?
        WHERE s.student_name = ?
        AND c1.time_to_start = c2.time_to_start;
    """;

        String addStudentQuery = """
        INSERT INTO course_students (student_id, course_id)
        VALUES (
            (SELECT id FROM students WHERE student_name = ?),
            (SELECT id FROM courses WHERE course_name = ?)
        );
    """;

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement conflictStmt = connection.prepareStatement(conflictQuery);
             PreparedStatement addStmt = connection.prepareStatement(addStudentQuery)) {

            // Çakışma kontrolü
            conflictStmt.setString(1, courseName);
            conflictStmt.setString(2, studentName);
            ResultSet resultSet = conflictStmt.executeQuery();

            if (resultSet.next()) {
                System.out.println("Conflict: The student already has a course at this time.");
                return false; // Çakışma var
            }

            // Öğrenciyi derse ekle
            addStmt.setString(1, studentName);
            addStmt.setString(2, courseName);
            int rowsAffected = addStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Student added successfully!");
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeStudentFromCourse(String studentName, String courseName) {
        String deleteSQL = """
        DELETE FROM course_students
        WHERE student_id = (SELECT id FROM students WHERE student_name = ?)
          AND course_id = (SELECT id FROM courses WHERE course_name = ?)
    """;

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {

            preparedStatement.setString(1, studentName);
            preparedStatement.setString(2, courseName);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Student removed from course successfully!");
                return true;
            } else {
                System.out.println("No matching record found to remove.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean reassignClassroomIfNeeded(
            String courseName,
            int newStudentCount,
            String timeToStart,
            Map<String, Set<String>> schedule) {

        String oldClassroom = getClassroomForCourse(courseName, timeToStart);
        if (oldClassroom != null && schedule.containsKey(oldClassroom)) {
            schedule.get(oldClassroom).remove(timeToStart);
        }

        String reassignment = SchoolManagementApp.findBestClassroom(courseName, newStudentCount, timeToStart, schedule);

        if (reassignment.contains("No suitable classroom")) {
            return false;
        } else {
            String newClassroom = parseClassroomFromResult(reassignment);
            return true;
        }
    }
    private static String getClassroomForCourse(String courseName, String timeToStart) {

        String query = """
        SELECT assigned_classroom 
        FROM course_assignments
        WHERE course_name = ? AND time_to_start = ?
        LIMIT 1;
    """;

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, courseName);
            ps.setString(2, timeToStart);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("assigned_classroom");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    private static String parseClassroomFromResult(String result) {

        String[] parts = result.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("Assigned Classroom: ")) {
                return part.replace("Assigned Classroom: ", "").trim();
            }
        }
        return null;
    }
    private static void updateCourseClassroomInDB(String courseName, String timeToStart, String newClassroom) {
        String updateQuery = """
        UPDATE course_assignments
        SET assigned_classroom = ?
        WHERE course_name = ? AND time_to_start = ?;
    """;

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            ps.setString(1, newClassroom);
            ps.setString(2, courseName);
            ps.setString(3, timeToStart);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean studentExists(String studentName) {
        String query = "SELECT 1 FROM students WHERE student_name = ? LIMIT 1";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, studentName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* public static Map<String, Map<String, String>> generateFullWeeklySchedule() {
         Map<String, Map<String, String>> weeklySchedule = new HashMap<>();

         try (Connection timetableConnection = DriverManager.getConnection("jdbc:sqlite:C:\\database\\TimetableManagement.db");
              Connection classroomConnection = DriverManager.getConnection("jdbc:sqlite:C:/database/ClassroomCapacity.db")) {

             // Fetch all courses with student counts and times
             String courseQuery = """
             SELECT courses.course_name, courses.time_to_start, courses.duration, COUNT(DISTINCT course_students.student_id) AS student_count
             FROM courses
             LEFT JOIN course_students ON courses.id = course_students.course_id
             GROUP BY courses.id;
         """;

             List<Map<String, Object>> courses = new ArrayList<>();
             try (PreparedStatement courseStatement = timetableConnection.prepareStatement(courseQuery);
                  ResultSet courseResultSet = courseStatement.executeQuery()) {

                 while (courseResultSet.next()) {
                     Map<String, Object> course = new HashMap<>();
                     course.put("course_name", courseResultSet.getString("course_name"));
                     course.put("time_to_start", courseResultSet.getString("time_to_start"));
                     course.put("duration", courseResultSet.getInt("duration"));
                     course.put("student_count", courseResultSet.getInt("student_count"));
                     courses.add(course);
                 }
             }

             // Fetch classrooms with capacities
             String classroomQuery = "SELECT Classroom, Capacity FROM classroom_capacity;";
             Map<String, Integer> classrooms = new HashMap<>();
             try (PreparedStatement classroomStatement = classroomConnection.prepareStatement(classroomQuery);
                  ResultSet classroomResultSet = classroomStatement.executeQuery()) {

                 while (classroomResultSet.next()) {
                     String classroom = classroomResultSet.getString("Classroom");
                     int capacity = classroomResultSet.getInt("Capacity");
                     classrooms.put(classroom, capacity);
                 }
             }

             // Assign courses to classrooms
             for (Map<String, Object> course : courses) {
                 String courseName = (String) course.get("course_name");
                 String timeToStart = (String) course.get("time_to_start");
                 int duration = (int) course.get("duration");
                 int studentCount = (int) course.get("student_count");

                 boolean assigned = false;

                 for (String classroom : classrooms.keySet()) {
                     if (classrooms.get(classroom) >= studentCount) {
                         weeklySchedule.computeIfAbsent(classroom, k -> new HashMap<>())
                                 .put(timeToStart, "Course: " + courseName + ", Duration: " + duration + " hours");
                         assigned = true;
                         break;
                     }
                 }

                 if (!assigned) {
                     weeklySchedule.computeIfAbsent("Unassigned", k -> new HashMap<>())
                             .put(timeToStart, "Course: " + courseName + ", Duration: " + duration + " hours");
                 }
             }

         } catch (SQLException e) {
             e.printStackTrace();
         }

         return weeklySchedule;
     }

     */
    public static Map<String, Map<String, String>> generateFullWeeklySchedule() {
        Map<String, Map<String, String>> weeklySchedule = new HashMap<>();

       /*try (Connection timetableConnection = DriverManager.getConnection("jdbc:sqlite:C:\\database\\TimetableManagement.db");
            Connection classroomConnection = DriverManager.getConnection("jdbc:sqlite:C:/database/ClassroomCapacity.db")) {

           // Fetch all courses with student counts and times
           String courseQuery = """
            SELECT courses.course_name, courses.time_to_start, courses.duration,
                   COUNT(DISTINCT course_students.student_id) AS student_count
            FROM courses
            LEFT JOIN course_students ON courses.id = course_students.course_id
            GROUP BY courses.id;
        """;


           List<Map<String, Object>> courses = new ArrayList<>();
           try (PreparedStatement courseStatement = timetableConnection.prepareStatement(courseQuery);
                ResultSet courseResultSet = courseStatement.executeQuery()) {

               while (courseResultSet.next()) {
                   Map<String, Object> course = new HashMap<>();
                   course.put("course_name", courseResultSet.getString("course_name"));
                   course.put("time_to_start", courseResultSet.getString("time_to_start"));
                   course.put("duration", courseResultSet.getInt("duration"));
                   course.put("student_count", courseResultSet.getInt("student_count"));
                   courses.add(course);
               }
           }
// Fetch classrooms with capacities
           String classroomQuery = "SELECT Classroom, Capacity FROM classroom_capacity;";
           Map<String, Integer> classrooms = new HashMap<>();
           try (PreparedStatement classroomStatement = classroomConnection.prepareStatement(classroomQuery);
                ResultSet classroomResultSet = classroomStatement.executeQuery()) {

               while (classroomResultSet.next()) {
                   String classroom = classroomResultSet.getString("Classroom");
                   int capacity = classroomResultSet.getInt("Capacity");
                   classrooms.put(classroom, capacity);
               }
           }

           // Initialize weekly schedule structure for each classroom
           for (String classroom : classrooms.keySet()) {
               weeklySchedule.put(classroom, new HashMap<>());
           }

           // Assign courses to classrooms
           for (Map<String, Object> course : courses) {
               String courseName = (String) course.get("course_name");
               String timeToStart = (String) course.get("time_to_start");
               int duration = (int) course.get("duration");
               int studentCount = (int) course.get("student_count");

               boolean assigned = false;


               for (String classroom : classrooms.keySet()) {
                   if (classrooms.get(classroom) >= studentCount) {
                       Map<String, String> classroomSchedule = weeklySchedule.get(classroom);

                       // Check for conflicts in the classroom schedule
                       if (!classroomSchedule.containsKey(timeToStart)) {
                           classroomSchedule.put(timeToStart, "Course: " + courseName + ", Duration: " + duration + " hours");
                           assigned = true;
                           break;
                       }
                   }
               }



               if (!assigned) {
                   // If no classroom is found, add to an "Unassigned" schedule
                   weeklySchedule.computeIfAbsent("Unassigned", k -> new HashMap<>())
                           .put(timeToStart, "Course: " + courseName + ", Duration: " + duration + " hours, Students: " + studentCount);
               }
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }



        */

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:C:/database/TimetableManagement.db")) {
            // İkinci veritabanını iliştir
            String attachQuery = "ATTACH 'C:/database/ClassroomCapacity.db' AS db2;";
            try (PreparedStatement attachStatement = connection.prepareStatement(attachQuery)) {
                attachStatement.execute();
            }

            // Birleştirilmiş veriler için SQL sorgusu
            String query = """
        SELECT courses.course_name, courses.time_to_start, courses.duration, 
               COUNT(DISTINCT course_students.student_id) AS student_count,
               db2.classroom_capacity.Classroom, db2.classroom_capacity.Capacity
        FROM courses
        LEFT JOIN course_students ON courses.id = course_students.course_id
        LEFT JOIN db2.classroom_capacity ON db2.classroom_capacity.Classroom = courses.classroom_name
        GROUP BY courses.id, db2.classroom_capacity.Classroom;
    """;

            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Veriler işleniyor...
                    System.out.println("Course: " + resultSet.getString("course_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



        return weeklySchedule;
    }



    /**
     * Dersin öğrenci sayısına göre sınıf atamasını gerçekleştirir.
     *
     * @param courseName   Ders adı
     * @param studentCount Öğrenci sayısı
     * @return Atanan sınıf adı
     */
    public static String assignClassroomForCourse(String courseName, int studentCount) {
        String assignedClassroom = "No suitable classroom found";
        try (Connection conn = DriverManager.getConnection(CLASSROOM_DB_PATH)) {
            if (conn != null) {
                String query = "SELECT Classroom FROM classroom_capacity WHERE Capacity >= ? ORDER BY Capacity ASC LIMIT 1";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, studentCount);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        assignedClassroom = rs.getString("Classroom");
                        System.out.println("Course: " + courseName + " assigned to " + assignedClassroom);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignedClassroom;
    }














}
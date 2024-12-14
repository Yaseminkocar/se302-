package com.example.se302;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TableViewer {
    public static void listCourses() {
        String sql = "SELECT * FROM courses";

        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            // Print column headers dynamically
            System.out.printf("%-5s %-20s %-15s %-20s %-10s%n",
                    "ID", "Course Name", "Course Code", "Instructor", "Credits");

            // Print table rows
            while (resultSet.next()) {
                System.out.printf("%-5d %-20s %-15s %-20s %-10d%n",
                        resultSet.getInt("id"),               // ID
                        resultSet.getString("course_name"),   // Course Name
                        resultSet.getString("course_code"),   // Course Code
                        resultSet.getString("instructor"),    // Instructor
                        resultSet.getInt("credits"));         // Credits
            }
        } catch (Exception e) {
            System.out.println("Error listing courses: " + e.getMessage());
        }
    }
}


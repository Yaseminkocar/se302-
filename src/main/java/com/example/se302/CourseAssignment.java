package com.example.se302;

import javafx.beans.property.SimpleStringProperty;

public class CourseAssignment {
    private final SimpleStringProperty courseName;
    private final SimpleStringProperty classroom;
    private final SimpleStringProperty time;

    public CourseAssignment(String courseName, String classroom, String time) {
        this.courseName = new SimpleStringProperty(courseName);
        this.classroom = new SimpleStringProperty(classroom);
        this.time = new SimpleStringProperty(time);
    }

    public String getCourseName() {
        return courseName.get();
    }

    public String getClassroom() {
        return classroom.get();
    }

    public String getTime() {
        return time.get();
    }
}


package com.example.se302;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Classroom {
    private String className;
    private int capacity;
    private ObservableList<Student> students;
    private ObservableList<Lesson> lessons;  // Derslerin listesi eklendi

    public Classroom(String className, int capacity, ObservableList<Lesson> lessons) {
        this.className = className;
        this.capacity = capacity;
        this.students = FXCollections.observableArrayList();
        this.lessons = lessons != null ? lessons : FXCollections.observableArrayList();  // Dersler null ise boş liste
    }

    public String getClassName() {
        return className;
    }

    public int getCapacity() {
        return capacity;
    }

    public ObservableList<Student> getStudents() {
        return students;
    }

    public ObservableList<Lesson> getLessons() {
        return lessons;
    }

    // Öğrenci eklemek için
    public boolean addStudent(Student student) {
        if (students.size() < capacity) {
            students.add(student);
            return true;
        }
        return false;
    }

    // Derse öğrenci eklemek
    public boolean addLesson(Lesson lesson) {
        if (!lessons.contains(lesson)) {
            lessons.add(lesson);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return className + " (Capacity: " + capacity + ")";
    }
}

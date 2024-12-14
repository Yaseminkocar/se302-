package com.example.se302;

public class Student {
    private String name;
    private String id;
    private String assignedClass;
    private String details;

    // Constructor
    public Student(String name, String id, String assignedClass, String details) {
        this.name = name;
        this.id = id;
        this.assignedClass = assignedClass;
        this.details = details;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssignedClass() {
        return assignedClass;
    }

    public void setAssignedClass(String assignedClass) {
        this.assignedClass = assignedClass;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", ID: " + id + ", Class: " + assignedClass + ", Details: " + details;
    }
}

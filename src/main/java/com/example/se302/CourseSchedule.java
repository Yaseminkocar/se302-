package com.example.se302;

public class CourseSchedule {
    private String time;
    private String monday;
    private String tuesday;
    private String wednesday;
    private String thursday;
    private String friday;

    public CourseSchedule(String time, String monday, String tuesday, String wednesday, String thursday, String friday) {
        this.time = time;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
    }

    public String getTime() { return time; }
    public String getMonday() { return monday; }
    public String getTuesday() { return tuesday; }
    public String getWednesday() { return wednesday; }
    public String getThursday() { return thursday; }
    public String getFriday() { return friday; }
}

package com.example.se302;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Lesson {
    private StringProperty lessonName; // Dersin adı
    private StringProperty startTime; // Başlangıç zamanı
    private StringProperty endTime;   // Bitiş zamanı

    public Lesson(String lessonName, String startTime, String endTime) {
        this.lessonName = new SimpleStringProperty(lessonName);
        this.startTime = new SimpleStringProperty(startTime);
        this.endTime = new SimpleStringProperty(endTime);
    }

    // Dersin adı
    public String getLessonName() {
        return lessonName.get();
    }

    public void setLessonName(String lessonName) {
        this.lessonName.set(lessonName);
    }

    public StringProperty lessonNameProperty() {
        return lessonName;
    }

    // Başlangıç zamanı
    public String getStartTime() {
        return startTime.get();
    }

    public void setStartTime(String startTime) {
        this.startTime.set(startTime);
    }

    public StringProperty startTimeProperty() {
        return startTime;
    }

    // Bitiş zamanı
    public String getEndTime() {
        return endTime.get();
    }

    public void setEndTime(String endTime) {
        this.endTime.set(endTime);
    }

    public StringProperty endTimeProperty() {
        return endTime;
    }

    @Override
    public String toString() {
        return lessonName.get() + " (" + startTime.get() + " - " + endTime.get() + ")";
    }
}



/*package com.example.se302;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Lesson {
    private StringProperty lessonName;
    private StringProperty schedule; // Dersin zamanı, örn: Pazartesi 09:00 - 12:00

    public Lesson(String lessonName, String schedule) {
        this.lessonName = new SimpleStringProperty(lessonName);
        this.schedule = new SimpleStringProperty(schedule);
    }

    public String getLessonName() {
        return lessonName.get();
    }

    public StringProperty lessonNameProperty() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName.set(lessonName);
    }

    public String getSchedule() {
        return schedule.get();
    }

    public StringProperty scheduleProperty() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule.set(schedule);
    }

    @Override
    public String toString() {
        return lessonName.get() + " - " + schedule.get();
    }
} */

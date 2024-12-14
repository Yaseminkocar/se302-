package com.example.se302;

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
}

package com.example.telehealth.models.patient;

public class MedicalCheckup {
    String doctor_id;
    String condition;
    String date_of_checkup;
    long time;

    public MedicalCheckup()
    {

    }

    public MedicalCheckup(String doctor_id, String condition, String date_of_checkup, long time)
    {
        this.doctor_id = doctor_id;
        this.condition = condition;
        this.date_of_checkup = date_of_checkup;
        this.time = time;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDate_of_checkup() {
        return date_of_checkup;
    }

    public void setDate_of_checkup(String date_of_checkup) {
        this.date_of_checkup = date_of_checkup;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

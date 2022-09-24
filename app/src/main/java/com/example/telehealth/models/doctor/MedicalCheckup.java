package com.example.telehealth.models.doctor;

public class MedicalCheckup {

    String patient_id;
    String condition;
    String date_of_checkup;
    long time;

    public MedicalCheckup()
    {

    }

    public MedicalCheckup(String patient_id, String condition, String date_of_checkup, long time)
    {
        this.patient_id = patient_id;
        this.condition = condition;
        this.date_of_checkup = date_of_checkup;
        this.time = time;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
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

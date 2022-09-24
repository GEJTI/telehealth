package com.example.telehealth.models;

public class Feedback {
    public String patient_id;
    public String feedback;
    public Boolean is_hidden;
    public long time_sent;
    public long inverse_timestamp;

    public Feedback()
    {

    }

    public Feedback(String patient_id, String feedback,
                    Boolean is_hidden,
                    long time_sent, long inverse_timestamp)
    {
        this.patient_id = patient_id;
        this.feedback = feedback;
        this.is_hidden = is_hidden;
        this.time_sent = time_sent;
        this.inverse_timestamp = inverse_timestamp;
    }

    public String getPatientId() {
        return patient_id;
    }

    public void setPatientId(String patient_id) {
        this.patient_id = patient_id;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Boolean getIsHidden() {
        return is_hidden;
    }

    public void setIsHidden(Boolean is_hidden) {
        this.is_hidden = is_hidden;
    }

    public long getTimeSent() {
        return time_sent;
    }

    public void setTimeSent(long time_sent) {
        this.time_sent = time_sent;
    }

    public long getInverseTimestamp() {
        return inverse_timestamp;
    }

    public void setInverseTimestamp(long inverse_timestamp) {
        this.inverse_timestamp = inverse_timestamp;
    }
}

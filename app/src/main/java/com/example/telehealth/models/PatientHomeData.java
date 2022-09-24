package com.example.telehealth.models;

public class PatientHomeData {
    public int imageId;
    public String category;

    public PatientHomeData(String category, int imageId){
        this.category = category;
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    public String getCategory() {
        return category;
    }
}

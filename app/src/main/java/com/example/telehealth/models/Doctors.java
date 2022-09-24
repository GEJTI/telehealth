package com.example.telehealth.models;

public class Doctors {
    public String name;
    public String category;
    public String specialization;
    public String mobile;
    public String licenseUrl;
    public Boolean online;
    public long lastOnline;
    public String availability;
    public String imageUrl;
    public String id;
    public String licenseValidity;

    public Doctors(){}

    public Doctors(String name,String category, String specialization, String mobile,
                   String licenseUrl, String imageUrl, Boolean online, long lastOnline, String availability, String licenseValidity){
        this.name = name;
        this.category = category;
        this.specialization = specialization;
        this.mobile = mobile;
        this.licenseUrl = licenseUrl;
        this.online = online;
        this.availability = availability;
        this.licenseUrl = licenseUrl;
        this.online = online;
        this.lastOnline = lastOnline;
        this.licenseValidity = licenseValidity;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public String getAvailability() {
        return (availability != null && !availability.equals(""))? availability : "Available";
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public void setLicenseValidity(String licenseValidity) {
        this.licenseValidity = licenseValidity;
    }

    public String getLicenseValidity() {
        return licenseValidity;
    }
}

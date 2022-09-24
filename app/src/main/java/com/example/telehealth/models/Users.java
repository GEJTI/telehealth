package com.example.telehealth.models;

public class Users {
    private String image, mobile, name, status, birthday, age, validIdUrl, gender;

    public Users(){

    }

    public Users(String image, String mobile, String name, String status, String birthday, String age, String validIdUrl, String gender) {
        this.image = image;
        this.mobile = mobile;
        this.name = name;
        this.status = status;
        this.birthday = birthday;
        this.age = age;
        this.validIdUrl = validIdUrl;
        this.gender = gender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getValidIdUrl() {
        return validIdUrl;
    }

    public void setValidIdUrl(String validIdUrl) {
        this.validIdUrl = validIdUrl;
    }
}

package com.example.midterm;

public class Student {
    private String id;
    private String name;
    private String study;
    private String classroom;
    private String course;
    private String gender;
    private String birth;
    private String address;
    private boolean option;

    public Student(String id, String name, String study, String classroom, String course, String gender, String birth, String address) {
        this.id = id;
        this.name = name;
        this.study = study;
        this.classroom = classroom;
        this.course = course;
        this.gender = gender;
        this.birth = birth;
        this.address = address;
    }

    public Student() {
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public boolean isOption() {
        return option;
    }

    public void setOption(boolean option) {
        this.option = option;
    }

    public String getGender() {
        return this.gender;
    }

    public String getCourse() {
        return this.course;
    }

    public String getClassroom() {
        return this.classroom;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getBirth() {
        return this.birth;
    }

    public String getAddress() {
        return this.address;
    }

    public String getStudy() {
        return this.study;
    }
}

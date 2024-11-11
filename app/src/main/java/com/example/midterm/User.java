package com.example.midterm;

public class User {

    private String documentId;
    private String name;
    private int age;
    private String phone;
    private String status;

    public User(String name, int age, String phone, String status) {
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.status = status;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }
}

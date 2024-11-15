package com.example.midterm;

public class User {

    private String documentId;
    private String name;
    private int age;
    private String phone;
    private String status;
    private String email;
    private String password;
    private String role;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public User(String name, int age, String phone, String status, String email, String password, String role) {
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.status = status;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String name, int age, String phone, String status, String role) {
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.status = status;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

package com.example.midterm;


import com.google.firebase.Timestamp;

public class LoginHistory {
    private String userId;
    private Timestamp timestamp;

    private String email;

    public LoginHistory(String userId, String email, Timestamp timestamp) {
        this.userId = userId;
        this.email = email;
        this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LoginHistory(String userId, Timestamp timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

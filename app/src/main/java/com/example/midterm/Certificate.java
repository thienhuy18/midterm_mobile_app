package com.example.midterm;

public class Certificate {
    private String id;
    private String title;
    private String studentid;
    private String date;
    private String detail;
    private boolean option;

    public Certificate(String id, String title, String studentid, String date, String detail) {
        this.id = id;
        this.title = title;
        this.studentid = studentid;
        this.date = date;
        this.detail = detail;
    }

    public Certificate() {}

    public String getId() { return this.id; }

    public String getDate() { return this.date; }

    public String getTitle() { return this.title; }

    public String getStudentid() { return this.studentid; }

    public String getDetail() { return this.detail; }

    public void setOption(boolean option) { this.option = option; }

    public boolean isOption() { return option; }


}

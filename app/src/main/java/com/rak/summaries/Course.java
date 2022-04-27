package com.rak.summaries;

public class Course {
    private final String name;
    private final String url;
    private final String number;
    private final boolean active;

    public Course(String name, String url, String number, boolean active) {
        this.name = name;
        this.url = url;
        this.number = number;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getNumber() {
        return number;
    }

    public boolean getActive() {return active;}
}

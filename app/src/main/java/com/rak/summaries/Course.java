package com.rak.summaries;

public class Course {
    private final String name;
    private final String url;
    private final String number;

    public Course(String name, String url, String number) {
        this.name = name;
        this.url = url;
        this.number = number;
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
}

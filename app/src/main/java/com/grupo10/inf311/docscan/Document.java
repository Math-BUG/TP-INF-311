package com.grupo10.inf311.docscan;

public class Document {
    private String name;
    private String date;

    public Document(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getName() { return name; }
    public String getDate() { return date; }
}

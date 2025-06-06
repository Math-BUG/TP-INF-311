package com.grupo10.inf311.docscan;

// Document.java
// Document.java
public class Document {
    private String id;
    private String name;
    private String date;
    private String imagePath; // Adicione o caminho da imagem

    public Document(String id, String name, String date, String imagePath) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.imagePath = imagePath;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getImagePath() { // Novo getter
        return imagePath;
    }
}
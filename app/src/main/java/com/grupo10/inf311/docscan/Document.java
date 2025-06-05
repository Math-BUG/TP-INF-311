package com.grupo10.inf311.docscan;

// Document.java
public class Document {
    private String id; // Adicione um ID único para o documento
    private String name;
    private String date;
    private boolean isSelected; // Para rastrear o estado de seleção

    public Document(String id, String name, String date) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.isSelected = false; // Por padrão, não selecionado
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

    public boolean isSelected() {
        return isSelected;
    }

    // Setters
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    // Você pode querer um método para gerar IDs únicos, por exemplo, usando UUID.randomUUID().toString();
}
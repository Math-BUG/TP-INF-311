package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;

// Representa os dados de uma pessoa encontrados na Rubeus
public class PersonData {
    @SerializedName("id")
    private String id; // O ID é a chave mais importante para o evento
    @SerializedName("nome")
    private String nome;
    @SerializedName("cpf")
    private String cpf;

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
}
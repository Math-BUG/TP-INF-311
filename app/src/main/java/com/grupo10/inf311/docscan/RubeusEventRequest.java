package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;

// Objeto que enviamos para criar um evento
public class RubeusEventRequest {

    // Classe interna para referenciar a pessoa pelo ID
    public static class Person {
        @SerializedName("id")
        String id;

        public Person(String id) { this.id = id; }
    }

    @SerializedName("codigo")
    String codigo;
    @SerializedName("tipo")
    int tipo;
    @SerializedName("descricao")
    String descricao;
    @SerializedName("pessoa")
    Person pessoa;
    @SerializedName("momento")
    String momento;
    @SerializedName("origem")
    int origem;
    @SerializedName("token")
    String token;

    public RubeusEventRequest(String codigo, int tipo, String descricao, Person pessoa, String momento, int origem, String token) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.descricao = descricao;
        this.pessoa = pessoa;
        this.momento = momento;
        this.origem = origem;
        this.token = token;
    }
}
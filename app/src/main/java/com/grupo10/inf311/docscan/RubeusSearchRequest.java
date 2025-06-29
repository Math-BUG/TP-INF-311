package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;

// Objeto que enviamos para a Rubeus para fazer uma busca
public class RubeusSearchRequest {
    @SerializedName("cpf")
    String cpf;
    @SerializedName("token")
    String token;
    @SerializedName("origem")
    int origem;

    public RubeusSearchRequest(String cpf, int origem, String token) {
        this.cpf = cpf;
        this.origem = origem;
        this.token = token;
    }
}
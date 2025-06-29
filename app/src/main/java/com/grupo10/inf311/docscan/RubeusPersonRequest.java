// RubeusPersonRequest.java
package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;

public class RubeusPersonRequest {
    @SerializedName("codigo")
    private String codigo;

    @SerializedName("nomeCompleto")
    private String nomeCompleto;

    @SerializedName("cpf")
    private String cpf;

    @SerializedName("origem")
    private Integer origem;

    @SerializedName("token")
    private String token;

    public RubeusPersonRequest(String codigo, String nomeCompleto, String cpf, Integer origem, String token) {
        this.codigo = codigo;
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.origem = origem;
        this.token = token;
    }

    // Getters and Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Integer getOrigem() { return origem; }
    public void setOrigem(Integer origem) { this.origem = origem; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
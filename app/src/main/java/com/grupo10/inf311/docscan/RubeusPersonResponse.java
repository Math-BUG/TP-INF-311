package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;

public class RubeusPersonResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PersonData data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public PersonData getData() { return data; }
    public void setData(PersonData data) { this.data = data; }

    public static class PersonData {
        @SerializedName("id")
        private String id;

        @SerializedName("codigo")
        private String codigo;

        @SerializedName("nomeCompleto")
        private String nomeCompleto;

        @SerializedName("cpf")
        private String cpf;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public String getNomeCompleto() { return nomeCompleto; }
        public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
    }
}
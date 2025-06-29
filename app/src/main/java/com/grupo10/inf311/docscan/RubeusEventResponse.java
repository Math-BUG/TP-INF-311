package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;

// A resposta simples da API de evento
public class RubeusEventResponse {

    @SerializedName("success") // O campo é "success" (em inglês)
    private boolean success;

    @SerializedName("dados") // A resposta contém um objeto "dados"
    private EventData dados;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public EventData getDados() {
        return dados;
    }

    public static class EventData {
        @SerializedName("id")
        private String id;
        @SerializedName("descricao")
        private String descricao;

        public String getId() {
            return id;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}
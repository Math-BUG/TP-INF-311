package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// A resposta completa da API de busca
public class RubeusSearchResponse {
    @SerializedName("dados")
    private List<PersonData> dados;

    public List<PersonData> getDados() { return dados; }
}
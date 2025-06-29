package com.grupo10.inf311.docscan;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RubeusApiService {

    // Endpoint para buscar contatos por CPF (ou nome, etc.)
    @POST("api/Contato/dadosPessoas")
    Call<RubeusSearchResponse> buscarPessoas(@Body RubeusSearchRequest request);

    // Endpoint para registrar um novo evento
    @POST("api/Evento/cadastro")
    Call<RubeusEventResponse> criarEvento(@Body RubeusEventRequest request);
}
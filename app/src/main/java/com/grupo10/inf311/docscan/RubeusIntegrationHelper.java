package com.grupo10.inf311.docscan;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class RubeusIntegrationHelper {
    private static final String TAG = "RubeusIntegrationHelper";

    private static final String API_TOKEN = "";//chave de api
    private static final Integer ORIGEM_APP = 0;//numero do canal da rubeus
    private static final int TIPO_EVENTO_VALIDACAO = 1;

    public interface RubeusCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public static void validarDocumentoEregistrarEvento(Map<String, String> dadosExtraidos, RubeusCallback callback) {
        Log.d(TAG, "Iniciando validação de documento...");

        String nomeExtraido = dadosExtraidos.get("NOME");
        String cpfExtraido = dadosExtraidos.get("cpf");

        if (nomeExtraido == null || nomeExtraido.trim().isEmpty() || cpfExtraido == null || cpfExtraido.trim().isEmpty()) {
            callback.onError("Nome ou CPF não puderam ser extraídos do documento.");
            return;
        }
        buscarPessoaPorCPF(cpfExtraido, nomeExtraido, callback);
    }

    private static void buscarPessoaPorCPF(String cpf, final String nomeExtraido, RubeusCallback callback) {
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        Log.d(TAG, "Passo 1: Buscando pessoa com CPF " + cpfLimpo);

        RubeusSearchRequest request = new RubeusSearchRequest(cpfLimpo, ORIGEM_APP, API_TOKEN);
        RubeusApiService apiService = RubeusRetrofitClient.getApiService();
        Call<RubeusSearchResponse> call = apiService.buscarPessoas(request);

        call.enqueue(new Callback<RubeusSearchResponse>() {
            @Override
            public void onResponse(Call<RubeusSearchResponse> call, Response<RubeusSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getDados() != null && !response.body().getDados().isEmpty()) {
                    PersonData pessoaDaApi = response.body().getDados().get(0);
                    validarNomeEregistrarEvento(pessoaDaApi, nomeExtraido, callback);
                } else {
                    callback.onError("Este CPF não corresponde a um contato cadastrado na plataforma.");
                }
            }

            @Override
            public void onFailure(Call<RubeusSearchResponse> call, Throwable t) {
                callback.onError("Falha na conexão ao buscar contato: " + t.getMessage());
            }
        });
    }

    private static void validarNomeEregistrarEvento(PersonData pessoaDaApi, String nomeExtraido, RubeusCallback callback) {
        Log.d(TAG, "Passo 2: Validando nome...");
        if (pessoaDaApi.getNome().equalsIgnoreCase(nomeExtraido)) {
            Log.i(TAG, "Validação de nome BEM-SUCEDIDA.");
            criarEventoDeValidacao(pessoaDaApi, callback);
        } else {
            callback.onError("CPF encontrado, mas o nome não corresponde.\nDoc: " + nomeExtraido + "\nPlataforma: " + pessoaDaApi.getNome());
        }
    }

    private static void criarEventoDeValidacao(PersonData pessoaValidada, RubeusCallback callback) {
        Log.d(TAG, "Passo 3: Criando evento de validação para " + pessoaValidada.getNome());

        String eventCode = "DOC_VALIDADO_" + UUID.randomUUID().toString().substring(0, 8);
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String description = "Documento de identidade validado com sucesso através do aplicativo DocScan.";

        RubeusEventRequest.Person pessoaParaEvento = new RubeusEventRequest.Person(pessoaValidada.getId());
        RubeusEventRequest eventRequest = new RubeusEventRequest(
                eventCode, TIPO_EVENTO_VALIDACAO, description, pessoaParaEvento, currentDateTime, ORIGEM_APP, API_TOKEN
        );

        RubeusApiService apiService = RubeusRetrofitClient.getApiService();
        Call<RubeusEventResponse> call = apiService.criarEvento(eventRequest);

        call.enqueue(new Callback<RubeusEventResponse>() {
            @Override
            public void onResponse(Call<RubeusEventResponse> call, Response<RubeusEventResponse> response) {
                // *** A LÓGICA FINAL E CORRETA ***
                // Com o molde RubeusEventResponse corrigido, esta condição agora será VERDADEIRA
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String eventoId = response.body().getDados().getId();
                    Log.i(TAG, "SUCESSO FINAL! Evento de validação registrado com ID: " + eventoId);
                    callback.onSuccess("Documento validado e evento registrado com sucesso!");
                } else {
                    Log.e(TAG, "Erro ao registrar evento de validação. Código: " + response.code());
                    callback.onError("A API não confirmou o sucesso do evento.");
                }
            }

            @Override
            public void onFailure(Call<RubeusEventResponse> call, Throwable t) {
                Log.e(TAG, "Falha de conexão ao criar evento.", t);
                callback.onError("Falha de rede ao registrar o evento: " + t.getMessage());
            }
        });
    }
}
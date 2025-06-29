package com.grupo10.inf311.docscan;

import android.util.Log;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * VERSÃO FINAL E CORRIGIDA
 * Ajusta o nome do campo na resposta de "data" para "dados" para corresponder
 * à resposta real da API Rubeus.
 */
public class RubeusValidationTester {

    // --- CONFIGURAÇÕES DO TESTE ---
    private static final String TAG = "RubeusValidationTester";
    private static final String BASE_URL = "https://crmufvgrupo10.apprubeus.com.br/";
    private static final String API_TOKEN = "b0cc357decf63354ffb0c3c6ba913f10";
    private static final int ORIGEM_APP = 8;
    private static final String NOME_PARA_BUSCAR = "MATEUS HENRIQUE DOS ANJOS OLIVEIRA";
    private static final String CPF_ESPERADO = "98765432100";
    // ------------------------------------

    // --- MODELOS DE DADOS E INTERFACE ---
    public static class SearchRequest {
        @SerializedName("nome") String nome;
        @SerializedName("token") String token;
        @SerializedName("origem") int origem;
    }

    public static class PersonData {
        @SerializedName("nome") private String nome;
        @SerializedName("cpf") private String cpf;
        public String getNome() { return nome; }
        public String getCpf() { return cpf; }
    }

    // A RESPOSTA COMPLETA DA API
    public static class SearchResponse {
        // AQUI ESTÁ A CORREÇÃO! MUDAMOS DE "data" para "dados"
        @SerializedName("dados")
        private List<PersonData> dados;

        public List<PersonData> getDados() { return dados; }
    }

    interface ValidationApiService {
        @POST("api/Contato/dadosPessoas")
        Call<SearchResponse> findPerson(@Body SearchRequest request);
    }
    // ------------------------------------


    // --- MÉTODO PRINCIPAL DE TESTE ---
    public static void executeTest() {
        Log.d(TAG, "--- EXECUTANDO TESTE DE VALIDAÇÃO CORRIGIDO ---");
        Log.d(TAG, "Procurando por: '" + NOME_PARA_BUSCAR + "'");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.NONE); // Não precisamos mais ver o JSON cru
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build();

        SearchRequest request = new SearchRequest();
        request.nome = NOME_PARA_BUSCAR;
        request.token = API_TOKEN;
        request.origem = ORIGEM_APP;

        ValidationApiService apiService = retrofit.create(ValidationApiService.class);
        Call<SearchResponse> call = apiService.findPerson(request);

        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                Log.d(TAG, "Servidor respondeu com código HTTP: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    SearchResponse searchResponse = response.body();
                    // AGORA ESTA CONDIÇÃO FUNCIONARÁ
                    if (searchResponse.getDados() != null && !searchResponse.getDados().isEmpty()) {
                        PersonData person = searchResponse.getDados().get(0);
                        Log.i(TAG, "✅✅✅ PESSOA ENCONTRADA COM SUCESSO! ✅✅✅");
                        Log.i(TAG, "   -> Nome retornado: " + person.getNome());
                        String cpfRetornadoLimpo = person.getCpf() != null ? person.getCpf().replaceAll("[^0-9]", "") : "";
                        if (CPF_ESPERADO.equals(cpfRetornadoLimpo)) {
                            Log.i(TAG, "   -> VALIDAÇÃO DO CPF CORRETA!");
                        } else {
                            Log.e(TAG, "   -> VALIDAÇÃO DO CPF FALHOU!");
                        }
                    } else {
                        Log.w(TAG, "⚠️ A busca funcionou, mas a lista de 'dados' veio vazia.");
                    }
                } else {
                    Log.e(TAG, "❌ ERRO! A API retornou um erro HTTP " + response.code());
                }
                Log.d(TAG, "--- FIM DO TESTE DE VALIDAÇÃO ---");
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e(TAG, "🔥🔥🔥 FALHA GERAL DE CONEXÃO! 🔥🔥🔥", t);
                Log.d(TAG, "--- FIM DO TESTE DE VALIDAÇÃO ---");
            }
        });
    }
}
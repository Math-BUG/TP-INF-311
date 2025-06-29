package com.grupo10.inf311.docscan;

import android.util.Log;
import com.google.gson.annotations.SerializedName;
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
 * Classe de teste focada em uma única coisa: TENTAR CRIAR UM CONTATO.
 * Usa um CPF matematicamente válido (mas fictício) e dados gerados na hora.
 */
public class RubeusCreationTester {

    // --- CONFIGURAÇÕES ---
    private static final String TAG = "RubeusCreationTester";
    private static final String BASE_URL = "https://crmufvgrupo10.apprubeus.com.br/";
    private static final String API_TOKEN = "";// coloque a chave de API aqui
    private static final int ORIGEM_APP = 8;// Origem do contato,canal da rubeus
    // ---------------------

    // --- MODELOS DE DADOS PARA A CRIAÇÃO ---

    // Modelo para a REQUISIÇÃO de criação, baseado na documentação
    public static class CreationRequest {
        @SerializedName("codigo")
        String codigo;
        @SerializedName("nome")
        String nome;
        @SerializedName("cpf")
        String cpf;
        @SerializedName("emailPrincipal") // Usando o nome correto do campo da API
        String emailPrincipal;
        @SerializedName("origem")
        int origem;
        @SerializedName("token")
        String token;
    }

    // Modelo para a RESPOSTA da API após a criação
    public static class CreationResponse {
        @SerializedName("success")
        private boolean success;
        @SerializedName("message")
        private String message;
        // Adicione outros campos se a resposta de sucesso contiver mais dados

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    // --- INTERFACE DA API PARA O RETROFIT ---
    interface CreationApiService {
        // Usando o endpoint de CADASTRO
        @POST("api/Contato/cadastro")
        Call<CreationResponse> createContact(@Body CreationRequest creationRequest);
    }

    // --- MÉTODO PRINCIPAL QUE EXECUTA O TESTE ---
    public static void executeTest() {
        Log.d(TAG, "--- INICIANDO TESTE DE CRIAÇÃO DE CONTATO ---");

        // 1. Geração de dados de teste únicos
        long timestamp = System.currentTimeMillis();
        String nomeTeste = "Contato de Teste " + timestamp;
        String emailTeste = "teste" + timestamp + "@docscan.app";
        // CPF matematicamente válido, mas fictício, para passar na validação da API
        String cpfValido = ""; // Exemplo de CPF válido (não real)
        String codigoUnico = "TESTE_APP_" + timestamp;

        Log.d(TAG, "Tentando criar contato: " + nomeTeste + " | CPF: " + cpfValido);

        // 2. Montando o objeto de requisição (o "corpo" do JSON)
        CreationRequest request = new CreationRequest();
        request.codigo = codigoUnico;
        request.nome = nomeTeste;
        request.cpf = cpfValido;
        request.emailPrincipal = emailTeste;
        request.origem = ORIGEM_APP;
        request.token = API_TOKEN;

        // 3. Configurando o Retrofit com o "espião" (ainda útil para ver o que enviamos)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 4. Criando e executando a chamada
        CreationApiService apiService = retrofit.create(CreationApiService.class);
        Call<CreationResponse> call = apiService.createContact(request);

        call.enqueue(new Callback<CreationResponse>() {
            @Override
            public void onResponse(Call<CreationResponse> call, Response<CreationResponse> response) {
                Log.d(TAG, "Servidor respondeu com o código HTTP: " + response.code());

                if (response.isSuccessful()) {
                    // CÓDIGO 2xx (200 OK ou 201 Created): SUCESSO!
                    Log.i(TAG, "✅✅✅ SUCESSO! A API aceitou a requisição de criação de contato. ✅✅✅");
                    CreationResponse responseBody = response.body();
                    if (responseBody != null) {
                        Log.i(TAG, "Mensagem do servidor: " + responseBody.getMessage());
                    } else {
                        Log.w(TAG, "A API retornou sucesso, mas com corpo vazio.");
                    }
                } else {
                    // CÓDIGO 4xx, 5xx: ERRO!
                    Log.e(TAG, "❌ ERRO! A API recusou a criação do contato.");
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Corpo do erro vazio.";
                        Log.e(TAG, "Detalhe do erro do servidor: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler a mensagem de erro da API.", e);
                    }
                }
                Log.d(TAG, "--- FIM DO TESTE DE CRIAÇÃO ---");
            }

            @Override
            public void onFailure(Call<CreationResponse> call, Throwable t) {
                Log.e(TAG, "🔥🔥🔥 FALHA GERAL DE CONEXÃO! 🔥🔥🔥", t);
                Log.d(TAG, "--- FIM DO TESTE DE CRIAÇÃO ---");
            }
        });
    }
}
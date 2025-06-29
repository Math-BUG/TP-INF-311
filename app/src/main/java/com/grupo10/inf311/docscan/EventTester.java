package com.grupo10.inf311.docscan;

import android.util.Log;
import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
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
 * Classe de teste focada em uma única coisa: TENTAR CRIAR UM EVENTO.
 * Usa o ID de um contato conhecido e o "espião" de rede para ver a resposta JSON exata.
 */
public class EventTester {

    // --- CONFIGURAÇÕES DO TESTE ---
    private static final String TAG = "RubeusEventTester";
    private static final String BASE_URL = "https://crmufvgrupo10.apprubeus.com.br/";
    private static final String API_TOKEN = "b0cc357decf63354ffb0c3c6ba913f10";
    private static final int ORIGEM_APP = 8;

    // --- DADOS PARA O CENÁRIO DE TESTE ---
    // ID do contato "MATEUS HENRIQUE DOS ANJOS OLIVEIRA" que encontramos na busca anterior.
    // A API espera o ID como String.
    private static final String ID_DA_PESSOA = "26";
    private static final int TIPO_EVENTO_VALIDACAO = 1; // Usando o tipo de evento 1
    // ------------------------------------

    // --- MODELOS DE DADOS E INTERFACE (autocontidos para o teste) ---

    // Requisição para criar o evento
    public static class EventRequest {
        // Classe interna para referenciar a pessoa pelo ID
        public static class Person {
            @SerializedName("id")
            String id;
            public Person(String id) { this.id = id; }
        }

        @SerializedName("codigo") String codigo;
        @SerializedName("tipo") int tipo;
        @SerializedName("descricao") String descricao;
        @SerializedName("pessoa") Person pessoa;
        @SerializedName("momento") String momento;
        @SerializedName("origem") int origem;
        @SerializedName("token") String token;
    }

    // Resposta genérica da API (só para a chamada do Retrofit funcionar)
    // O que nos importa é o JSON que o "espião" vai mostrar no Logcat.
    public static class EventResponse {
        // Deixamos vazio de propósito
    }

    // Interface com o endpoint de criação de evento
    interface EventApiService {
        @POST("api/Evento/cadastro")
        Call<EventResponse> createEvent(@Body EventRequest request);
    }
    // ----------------------------------------------------------------

    // --- MÉTODO PRINCIPAL QUE EXECUTA O TESTE ---
    public static void executeTest() {
        Log.d(TAG, "--- INICIANDO TESTE DE CRIAÇÃO DE EVENTO ---");
        Log.d(TAG, "Tentando criar evento para a pessoa com ID: " + ID_DA_PESSOA);

        // Configurando o Retrofit com o "espião" de logs
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Montando a requisição para CRIAR O EVENTO
        String eventCode = "EVENTO_TESTE_" + UUID.randomUUID().toString().substring(0, 8);
        // Usando o formato de data CORRIGIDO (com espaço)
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String description = "Teste de criação de evento de validação.";

        EventRequest.Person pessoaParaEvento = new EventRequest.Person(ID_DA_PESSOA);
        EventRequest request = new EventRequest();
        request.codigo = eventCode;
        request.tipo = TIPO_EVENTO_VALIDACAO;
        request.descricao = description;
        request.pessoa = pessoaParaEvento;
        request.momento = currentDateTime;
        request.origem = ORIGEM_APP;
        request.token = API_TOKEN;

        // Criando e executando a chamada
        EventApiService apiService = retrofit.create(EventApiService.class);
        Call<EventResponse> call = apiService.createEvent(request);

        call.enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                Log.d(TAG, "O servidor de eventos respondeu com o código HTTP: " + response.code());
                Log.d(TAG, "A resposta completa (com o JSON) deve estar nos logs com a tag 'OkHttp'.");
                Log.d(TAG, "--- FIM DO TESTE ---");
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Log.e(TAG, "🔥🔥🔥 FALHA GERAL DE CONEXÃO AO CRIAR EVENTO! 🔥🔥🔥", t);
                Log.d(TAG, "--- FIM DO TESTE ---");
            }
        });
    }
}
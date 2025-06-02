package com.grupo10.inf311.docscan;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LanguageToolActivity extends AppCompatActivity {

    private static final String TAG = "LanguageToolActivity"; // Tag para logs

    private EditText editTextTextToCorrect;
    private Button buttonCorrectText;
    private TextView textViewResults;

    private LanguageToolApiService apiService;

    // Defina o idioma alvo aqui. Exemplos: "pt-BR" (Português Brasil), "en-US" (Inglês EUA), "es" (Espanhol)
    private final String targetLanguage = "pt-BR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Certifique-se de que o nome do seu arquivo de layout corresponde
        setContentView(R.layout.activity_language_tool);

        // Inicializa as views
        editTextTextToCorrect = findViewById(R.id.editTextTextToCorrect);
        buttonCorrectText = findViewById(R.id.buttonCorrectText);
        textViewResults = findViewById(R.id.textViewResults);

        // Obtém a instância do serviço da API através do nosso RetrofitClient
        apiService = RetrofitClient.getApiService();

        // Configura o listener de clique para o botão
        buttonCorrectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToCorrect = editTextTextToCorrect.getText().toString().trim();
                if (!textToCorrect.isEmpty()) {
                    textViewResults.setText("Verificando..."); // String hardcoded
                    performSpellCheck(textToCorrect);
                } else {
                    Toast.makeText(LanguageToolActivity.this, "Por favor, insira um texto.", Toast.LENGTH_SHORT).show(); // String hardcoded
                }
            }
        });
    }

    private void performSpellCheck(final String text) {
        // Cria a chamada à API usando o serviço Retrofit
        Call<LanguageToolResponse> call = apiService.checkText(targetLanguage, text);

        // Executa a chamada de forma assíncrona (em uma thread de background)
        call.enqueue(new Callback<LanguageToolResponse>() {
            @Override
            public void onResponse(@NonNull Call<LanguageToolResponse> call, @NonNull Response<LanguageToolResponse> response) {
                // Chamado quando a API retorna uma resposta (bem-sucedida ou não)
                if (response.isSuccessful() && response.body() != null) {
                    // A requisição foi bem-sucedida e temos um corpo na resposta
                    LanguageToolResponse apiResponse = response.body();
                    processApiResponse(apiResponse, text);
                } else {
                    // A requisição foi feita, mas a API retornou um erro (ex: código 4xx ou 5xx)
                    String errorMessage = "Erro na API: " + response.code(); // String hardcoded
                    try {
                        if (response.errorBody() != null) {
                            // Tenta ler a mensagem de erro do corpo da resposta de erro
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody da resposta da API", e);
                    }
                    Log.e(TAG, "Erro da API: " + errorMessage);
                    textViewResults.setText(errorMessage);
                    Toast.makeText(LanguageToolActivity.this, "Erro da API: " + response.code(), Toast.LENGTH_LONG).show(); // String hardcoded
                }
            }

            @Override
            public void onFailure(@NonNull Call<LanguageToolResponse> call, @NonNull Throwable t) {
                // Chamado quando ocorre uma falha na comunicação com a API
                // (ex: sem conexão com a internet, timeout, problema de DNS)
                Log.e(TAG, "Falha na requisição LanguageTool: " + t.getMessage(), t);
                textViewResults.setText("Falha na requisição: " + t.getMessage()); // String hardcoded
                Toast.makeText(LanguageToolActivity.this, "Erro de conexão. Verifique sua internet.", Toast.LENGTH_LONG).show(); // String hardcoded
            }
        });
    }

    private void processApiResponse(LanguageToolResponse apiResponse, String originalText) {
        if (apiResponse.getMatches() == null || apiResponse.getMatches().isEmpty()) {
            textViewResults.setText("Nenhum erro encontrado.\n\n" +
                    "Texto original:\n" + originalText); // Strings hardcoded
            return;
        }

        StringBuilder suggestionsBuilder = new StringBuilder();
        StringBuilder correctedTextBuilder = new StringBuilder(originalText);

        // Para aplicar correções, precisamos iterar de trás para frente para que os índices não mudem.
        // Criamos uma lista de "Correções" para ordenar e aplicar.
        List<Correction> corrections = new ArrayList<>();

        for (LanguageToolResponse.Match match : apiResponse.getMatches()) {
            String originalWordSegment;
            // O offset do match é relativo ao texto original.
            originalWordSegment = originalText.substring(match.getOffset(), match.getOffset() + match.getLength());

            suggestionsBuilder.append("Erro: '").append(originalWordSegment).append("'"); // String hardcoded
            if (match.getRule() != null && match.getRule().getId() != null) {
                suggestionsBuilder.append(" (Regra: ").append(match.getRule().getId()).append(")"); // String hardcoded
            }
            suggestionsBuilder.append("\n");
            suggestionsBuilder.append("  ").append("Mensagem: ").append(match.getMessage()).append("\n"); // String hardcoded

            if (match.getReplacements() != null && !match.getReplacements().isEmpty() && match.getReplacements().get(0).getValue() != null) {
                String firstReplacement = match.getReplacements().get(0).getValue();
                suggestionsBuilder.append("  ").append("Sugestão: ").append(firstReplacement).append("\n"); // String hardcoded
                corrections.add(new Correction(match.getOffset(), match.getLength(), firstReplacement));
            } else {
                suggestionsBuilder.append("  ").append("(Nenhuma sugestão automática)").append("\n"); // String hardcoded
            }
            suggestionsBuilder.append("\n");
        }

        // Aplicar correções de trás para frente para não bagunçar os índices
        Collections.sort(corrections, new Comparator<Correction>() {
            @Override
            public int compare(Correction c1, Correction c2) {
                // Ordena por offset decrescente
                return Integer.compare(c2.offset, c1.offset);
            }
        });

        for (Correction correction : corrections) {
            correctedTextBuilder.replace(correction.offset, correction.offset + correction.length, correction.replacement);
        }

        textViewResults.setText("Sugestões:\n" + suggestionsBuilder.toString() + // String hardcoded
                "\n\n" + "Texto (tentativa de correção automática):\n" + correctedTextBuilder.toString()); // String hardcoded
    }

    // Classe auxiliar interna para guardar informações de correção para ordenação
    private static class Correction {
        int offset;
        int length;
        String replacement;

        Correction(int offset, int length, String replacement) {
            this.offset = offset;
            this.length = length;
            this.replacement = replacement;
        }
    }
}
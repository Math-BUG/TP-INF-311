package com.grupo10.inf311.docscan;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Versão Final da Activity de Processamento.
 * Conecta a UI com a lógica final do RubeusIntegrationHelper.
 */
public class RgProcessingActivity extends AppCompatActivity {

    private static final String TAG = "RgProcessingActivity";
    public static final String EXTRA_IMAGE_PATH = "EXTRA_IMAGE_PATH";

    private ImageView imgDocument;
    private ProgressBar progressBar;
    private TextView tvResult;
    private Button btnRetry;
    private Button btnSendToRubeus;

    private TextRecognizer textRecognizer;
    private ExecutorService executorService;
    private Handler mainHandler;
    private Map<String, String> extractedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rg_processing);

        initializeViews();
        initializeMLKit();
        setupListeners(); // Configura os cliques dos botões

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (imagePath != null) {
            loadAndDisplayImage(imagePath);
            startOcrProcessing(imagePath);
        } else {
            showError("Erro fatal: Caminho da imagem não foi fornecido.");
        }
    }

    private void initializeViews() {
        imgDocument = findViewById(R.id.imgDocument);
        progressBar = findViewById(R.id.progressBar);
        tvResult = findViewById(R.id.tvResult);
        btnRetry = findViewById(R.id.btnRetry);
        btnSendToRubeus = findViewById(R.id.btnSendToRubeus);
    }

    private void initializeMLKit() {
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void setupListeners() {
        btnRetry.setOnClickListener(v -> retryCapture());
        // Define a ação para o botão de validar/enviar
        btnSendToRubeus.setOnClickListener(v -> onValidateAndSendClicked());
    }

    private void loadAndDisplayImage(String imagePath) {
        try {
            imgDocument.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar imagem do caminho", e);
            showError("Não foi possível exibir a imagem processada.");
        }
    }

    private void startOcrProcessing(String imagePath) {
        setUiState(true, "Processando documento...");
        executorService.execute(() -> {
            try {
                InputImage inputImage = InputImage.fromFilePath(this, Uri.fromFile(new File(imagePath)));
                textRecognizer.process(inputImage)
                        .addOnSuccessListener(visionText -> {
                            extractedData = RgDataExtractor.extractRgData(visionText.getText());
                            mainHandler.post(this::displayOcrResults);
                        })
                        .addOnFailureListener(e -> mainHandler.post(() -> showError("Erro na leitura do documento.")));
            } catch (IOException e) {
                mainHandler.post(() -> showError("Erro ao preparar a imagem para leitura."));
            }
        });
    }

    private void displayOcrResults() {
        setUiState(false, "");
        if (extractedData != null && extractedData.get("NOME") != null && !extractedData.get("NOME").equals("Nome não encontrado")) {
            btnSendToRubeus.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.VISIBLE);
            StringBuilder resultText = new StringBuilder("=== DADOS EXTRAÍDOS ===\n\n");
            resultText.append("📋 NOME: ").append(extractedData.get("NOME")).append("\n\n");
            resultText.append("📄 CPF: ").append(extractedData.getOrDefault("cpf", "Não encontrado")).append("\n");
            tvResult.setText(resultText.toString());
        } else {
            showError("Não foi possível extrair os dados. Por favor, tente uma nova foto.");
        }
    }

    private void onValidateAndSendClicked() {
        if (extractedData == null || extractedData.isEmpty()) {
            Toast.makeText(this, "Não há dados extraídos para validar.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Validação")
                .setMessage("Deseja validar este documento na plataforma Rubeus? A ação registrará um evento de validação para a pessoa encontrada.")
                .setPositiveButton("Sim, Validar", (dialog, which) -> processRubeusIntegration())
                .setNegativeButton("Não", null)
                .show();
    }

    private void processRubeusIntegration() {
        setUiState(true, "Validando na Rubeus...");
        btnSendToRubeus.setEnabled(false);

        RubeusIntegrationHelper.validarDocumentoEregistrarEvento(extractedData, new RubeusIntegrationHelper.RubeusCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    setUiState(false, "");
                    StringBuilder resultText = new StringBuilder("");
                    resultText.append("✅ VALIDADO com sucesso!\n\n");
                    tvResult.setText(resultText.toString());
                    btnSendToRubeus.setText("✅ VALIDADO");
                    Toast.makeText(RgProcessingActivity.this, "Sucesso! " + message, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setUiState(false, "");
                    btnSendToRubeus.setEnabled(true);
                    new AlertDialog.Builder(RgProcessingActivity.this)
                            .setTitle("Falha na Validação")
                            .setMessage(error)
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }

    private void showError(String message) {
        setUiState(false, "❌ " + message);
        btnRetry.setVisibility(View.VISIBLE);
        btnSendToRubeus.setVisibility(View.GONE);
    }

    private void retryCapture() {
        Intent intent = new Intent(this, DocumentScanActivity.class);
        startActivity(intent);
        finish();
    }

    private void setUiState(boolean isLoading, String message) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        tvResult.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        textRecognizer.close();
    }
}
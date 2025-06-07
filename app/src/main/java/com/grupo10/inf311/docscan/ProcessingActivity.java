package com.grupo10.inf311.docscan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ProcessingActivity extends AppCompatActivity {

    private static final String TAG = "ProcessingActivity";
    private static final long MINIMUM_LOADING_TIME_MS = 2500; // 2.5 segundos

    private LinearLayout processingLayout;
    private LinearLayout resultLayout;
    private ImageButton btnCancel;
    private Button btnExport, btnViewDocument, btnTertiaryAction;

    private TextRecognizer textRecognizer;
    private String ocrResultText = ""; // Para armazenar o resultado do OCR

    // Flags para controlar o estado
    private boolean isOcrComplete = false;
    private boolean isMinimumTimePassed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        initViews();
        setupListeners();

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Pega a imagem da activity anterior
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(OcrActivity.EXTRA_IMAGE_URI)) {
            String imagePath = intent.getStringExtra(OcrActivity.EXTRA_IMAGE_URI);
            if (imagePath != null && !imagePath.isEmpty()) {
                startProcessing(Uri.parse(imagePath));
            } else {
                showErrorAndFinish("Caminho da imagem inválido.");
            }
        } else {
            showErrorAndFinish("Nenhuma imagem fornecida.");
        }
    }

    private void initViews() {
        processingLayout = findViewById(R.id.processing_layout);
        resultLayout = findViewById(R.id.result_layout);
        btnCancel = findViewById(R.id.btn_cancel_processing);
        btnExport = findViewById(R.id.btn_export);
        btnViewDocument = findViewById(R.id.btn_view_document);
        btnTertiaryAction = findViewById(R.id.btn_tertiary_action);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish()); // Fecha a tela de processamento

// Dentro da sua ProcessingActivity.java

        btnExport.setOnClickListener(v -> {
            // 1. Verifica se o resultado do OCR não está vazio
            if (ocrResultText != null && !ocrResultText.isEmpty()) {

                // 2. Prepara o texto para o Toast (os primeiros 50 caracteres)
                String previewText = ocrResultText.substring(0, Math.min(ocrResultText.length(), 50));

                // 3. Mostra o Toast (com a sintaxe corrigida)
                Toast.makeText(this, "Exportando: " + previewText, Toast.LENGTH_SHORT).show();

                // Aqui você adicionaria a lógica real de exportação,
                // como salvar em um arquivo de texto ou compartilhar.

            } else {
                // Informa ao usuário que não há nada para exportar
                Toast.makeText(this, "Nenhum texto para exportar.", Toast.LENGTH_SHORT).show();
            }
        });
        btnViewDocument.setOnClickListener(v -> {
            // Lógica para visualizar o documento completo (pode abrir uma nova tela)
            Toast.makeText(this, "Visualizando o documento...", Toast.LENGTH_SHORT).show();
        });

        btnTertiaryAction.setOnClickListener(v -> {
            Toast.makeText(this, "Ação terciária...", Toast.LENGTH_SHORT).show();
        });
    }

    private void startProcessing(Uri imageUri) {
        // 1. Inicia o temporizador mínimo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isMinimumTimePassed = true;
            showResultViewIfReady();
        }, MINIMUM_LOADING_TIME_MS);

        // 2. Inicia o processamento do OCR
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        Log.i(TAG, "OCR concluído com sucesso.");
                        ocrResultText = visionText.getText();
                        isOcrComplete = true;
                        showResultViewIfReady();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Falha no OCR: ", e);
                        ocrResultText = "Erro ao processar OCR: " + e.getMessage();
                        isOcrComplete = true; // Mesmo em falha, consideramos "completo" para sair do loading
                        showResultViewIfReady();
                    });
        } catch (IOException e) {
            Log.e(TAG, "Erro ao carregar imagem: ", e);
            ocrResultText = "Erro ao carregar a imagem.";
            isOcrComplete = true; // Consideramos completo para sair do loading
            showResultViewIfReady();
        }
    }

    private synchronized void showResultViewIfReady() {
        // Só mostra a tela de resultado se AMBOS, o OCR e o tempo mínimo, tiverem terminado
        if (isOcrComplete && isMinimumTimePassed) {
            processingLayout.setVisibility(View.GONE);
            resultLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}
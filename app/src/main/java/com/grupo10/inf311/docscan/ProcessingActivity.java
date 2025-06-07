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
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ProcessingActivity extends AppCompatActivity {

    private static final String TAG = "ProcessingActivity";
    private static final long MINIMUM_LOADING_TIME_MS = 2500;

    private LinearLayout processingLayout;
    private LinearLayout resultLayout;
    private TextRecognizer textRecognizer;
    private String ocrResultText = "";

    private boolean isOcrComplete = false;
    private boolean isMinimumTimePassed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        initViews();

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

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
        ImageButton btnCancel = findViewById(R.id.btn_cancel_processing);
        Button btnExport = findViewById(R.id.btn_export);
        Button btnViewDocument = findViewById(R.id.btn_view_document);

        btnCancel.setOnClickListener(v -> finish());

        btnViewDocument.setOnClickListener(v -> {
            if (ocrResultText == null || ocrResultText.isEmpty()) {
                Toast.makeText(this, "Nenhum texto de OCR para salvar.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveResultAndReturnHome(ocrResultText);
        });

        btnExport.setOnClickListener(v -> {
            if (ocrResultText != null && !ocrResultText.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, ocrResultText);
                startActivity(Intent.createChooser(shareIntent, "Exportar Texto"));
            } else {
                Toast.makeText(this, "Nenhum texto para exportar.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveResultAndReturnHome(String textToSave) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "OCR_" + timeStamp + ".txt";

        File file = new File(getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(textToSave.getBytes());

            String newDocId = UUID.randomUUID().toString();
            String newDocName = "Doc " + timeStamp;
            String newDocDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            String newDocPath = file.getAbsolutePath();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(HomeActivity.EXTRA_NEW_DOC_ID, newDocId);
            intent.putExtra(HomeActivity.EXTRA_NEW_DOC_NAME, newDocName);
            intent.putExtra(HomeActivity.EXTRA_NEW_DOC_DATE, newDocDate);
            intent.putExtra(HomeActivity.EXTRA_NEW_DOC_PATH, newDocPath);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            finish();

        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar arquivo de texto.", e);
            Toast.makeText(this, "Falha ao salvar o documento.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startProcessing(Uri imageUri) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isMinimumTimePassed = true;
            showResultViewIfReady();
        }, MINIMUM_LOADING_TIME_MS);

        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        ocrResultText = visionText.getText();
                        isOcrComplete = true;
                        showResultViewIfReady();
                    })
                    .addOnFailureListener(e -> {
                        ocrResultText = "Erro ao processar OCR: " + e.getMessage();
                        isOcrComplete = true;
                        showResultViewIfReady();
                    });
        } catch (IOException e) {
            ocrResultText = "Erro ao carregar a imagem.";
            isOcrComplete = true;
            showResultViewIfReady();
        }
    }

    private synchronized void showResultViewIfReady() {
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
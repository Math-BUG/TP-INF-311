package com.grupo10.inf311.docscan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ProcessingActivity extends AppCompatActivity {

    private static final String TAG = "ProcessingActivity";
    public static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";
    public static final String RESULT_OCR_TEXT = "RESULT_OCR_TEXT"; // Chave para devolver o texto

    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_IMAGE_URI)) {
            String imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI);
            if (imageUriString != null && !imageUriString.isEmpty()) {
                startProcessing(Uri.parse(imageUriString));
            } else {
                finishWithError("Caminho da imagem inválido.");
            }
        } else {
            finishWithError("Nenhuma imagem fornecida.");
        }
    }

    private void startProcessing(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        // SUCESSO: Devolve o texto encontrado
                        String resultText = visionText.getText();
                        finishWithSuccess(resultText);
                    })
                    .addOnFailureListener(e -> {
                        // FALHA: Devolve uma mensagem de erro
                        Log.e(TAG, "Falha no OCR", e);
                        finishWithError("Falha no OCR: " + e.getMessage());
                    });
        } catch (IOException e) {
            Log.e(TAG, "Erro ao carregar imagem para OCR", e);
            finishWithError("Erro ao carregar a imagem.");
        }
    }

    // Método para finalizar a activity e devolver o resultado com sucesso
    private void finishWithSuccess(String ocrText) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_OCR_TEXT, ocrText);
        setResult(Activity.RESULT_OK, resultIntent);
        finish(); // Fecha esta activity e volta para a anterior
    }

    // Método para finalizar a activity e devolver um erro
    private void finishWithError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_OCR_TEXT, errorMessage); // Opcional: devolver a msg de erro
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish(); // Fecha esta activity e volta para a anterior
    }
}
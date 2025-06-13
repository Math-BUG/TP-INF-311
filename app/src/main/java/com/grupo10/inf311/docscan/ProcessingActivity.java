package com.grupo10.inf311.docscan;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ProcessingActivity extends AppCompatActivity {

    private static final String TAG = "ProcessingActivity";
    public static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";
    public static final String RESULT_OCR_TEXT = "RESULT_OCR_TEXT";


    private static final long MIN_DISPLAY_TIME_MS = 1500; // 1.5 segundos

    private TextRecognizer textRecognizer;
    private long startTime; // Variável para guardar o tempo de início

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_processing);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        ImageView processingIcon = findViewById(R.id.processing_icon);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(processingIcon, "rotation", 0f, 360f);
        rotation.setDuration(2000);
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.setInterpolator(new LinearInterpolator());


        rotation.start();

        Drawable drawable = processingIcon.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_IMAGE_URI)) {
            String imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI);
            if (imageUriString != null && !imageUriString.isEmpty()) {

                startTime = System.currentTimeMillis();
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
            InputImage image = InputImage.fromFilePath(this, imageUri);

            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String resultText = visionText.getText();
                        handleResult(true, resultText);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Falha no OCR", e);
                        handleResult(false, "Falha no OCR: " + e.getMessage());
                    });
        } catch (IOException e) {
            Log.e(TAG, "Erro ao carregar imagem para OCR", e);
            handleResult(false, "Erro ao carregar a imagem.");
        }
    }


    private void handleResult(boolean isSuccess, String resultText) {
        long elapsedTime = System.currentTimeMillis() - startTime;

        if (elapsedTime < MIN_DISPLAY_TIME_MS) {
            // Se o processamento foi rápido demais, calcula o delay necessário
            long delayNeeded = MIN_DISPLAY_TIME_MS - elapsedTime;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isSuccess) {
                    finishWithSuccess(resultText);
                } else {
                    finishWithError(resultText);
                }
            }, delayNeeded);
        } else {
            // Se já demorou o suficiente, finaliza imediatamente
            if (isSuccess) {
                finishWithSuccess(resultText);
            } else {
                finishWithError(resultText);
            }
        }
    }

    private void finishWithSuccess(String ocrText) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_OCR_TEXT, ocrText);
        setResult(Activity.RESULT_OK, resultIntent);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void finishWithError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_OCR_TEXT, errorMessage);
        setResult(Activity.RESULT_CANCELED, resultIntent);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
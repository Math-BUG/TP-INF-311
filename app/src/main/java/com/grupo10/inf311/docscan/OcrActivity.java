package com.grupo10.inf311.docscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.File;
import java.io.IOException;

public class OcrActivity extends AppCompatActivity {

    private static final String TAG = "OcrActivity";
    public static final String EXTRA_IMAGE_URI = "com.grupo10.inf311.docscan.EXTRA_IMAGE_URI";

    private Button buttonCaptureImage, buttonSelectImage, buttonSelectPdf;
    private ImageView imageViewPreview;
    private TextView textViewOcrResult;
    private TextRecognizer textRecognizer;
    private Uri tempImageUri;

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) launchCamera();
                else Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result && tempImageUri != null) {
                    imageViewPreview.setImageURI(tempImageUri);
                    performOcr(tempImageUri);
                }
            });

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageViewPreview.setImageURI(uri);
                    performOcr(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSelectPdf = findViewById(R.id.buttonSelectPdf);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        textViewOcrResult = findViewById(R.id.textViewOcrResult);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        handleIncomingIntent();

        buttonCaptureImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        buttonSelectImage.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_IMAGE_URI)) {
            String imagePath = intent.getStringExtra(EXTRA_IMAGE_URI);
            if (imagePath != null && !imagePath.isEmpty()) {
                Uri imageUri = Uri.parse(imagePath);
                imageViewPreview.setImageURI(imageUri);
                performOcr(imageUri);
                buttonCaptureImage.setVisibility(View.GONE);
                buttonSelectImage.setVisibility(View.GONE);
                buttonSelectPdf.setVisibility(View.GONE);
            }
        }
    }

    private void launchCamera() {
        // Implementação do launchCamera
    }

    private void performOcr(Uri imageUri) {
        textViewOcrResult.setText("Processando OCR...");
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        textViewOcrResult.setText(visionText.getText());
                        if (visionText.getText().isEmpty()){
                            Toast.makeText(OcrActivity.this, "Nenhum texto encontrado.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        textViewOcrResult.setText("Falha no OCR: " + e.getMessage());
                    });
        } catch (IOException e) {
            textViewOcrResult.setText("Erro ao carregar imagem para OCR.");
        }
    }
}
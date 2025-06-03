package com.grupo10.inf311.docscan;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OcrActivity extends AppCompatActivity {

    private static final String TAG = "OcrActivity";

    private Button buttonCaptureImage, buttonSelectImage, buttonSelectPdf;
    private ImageView imageViewPreview;
    private TextView textViewOcrResult;
    private TextRecognizer textRecognizer;

    private Uri tempImageUri; // Para armazenar a URI da imagem capturada pela câmera

    // ActivityResultLauncher para permissão da câmera
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show();
                }
            });

    // ActivityResultLauncher para capturar imagem com a câmera
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result && tempImageUri != null) { // 'result' é true se a foto foi tirada e salva
                    imageViewPreview.setImageURI(tempImageUri);
                    performOcr(tempImageUri);
                } else {
                    Toast.makeText(this, "Captura de imagem cancelada ou falhou.", Toast.LENGTH_SHORT).show();
                }
            });

    // ActivityResultLauncher para selecionar imagem da galeria
    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageViewPreview.setImageURI(uri);
                    performOcr(uri);
                } else {
                    Toast.makeText(this, "Seleção de imagem cancelada.", Toast.LENGTH_SHORT).show();
                }
            });

    // ActivityResultLauncher para selecionar PDF
    private final ActivityResultLauncher<String[]> selectPdfLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    Toast.makeText(this, "PDF Selecionado: " + uri.getPath(), Toast.LENGTH_SHORT).show();
                    imageViewPreview.setImageResource(R.drawable.edittext_border_background); // Placeholder ou icone de PDF
                    convertPdfPageToImageAndPerformOcr(uri, 0); // Processa a primeira página (índice 0)
                } else {
                    Toast.makeText(this, "Seleção de PDF cancelada.", Toast.LENGTH_SHORT).show();
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

        // Inicializa o TextRecognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        buttonCaptureImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        buttonSelectImage.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

        buttonSelectPdf.setOnClickListener(v -> selectPdfLauncher.launch(new String[]{"application/pdf"}));
    }

    private void launchCamera() {
        File tempFile;
        try {
            // Cria um arquivo temporário para a imagem da câmera
            // É importante configurar o FileProvider corretamente no AndroidManifest.xml e criar res/xml/file_paths.xml
            // para evitar FileUriExposedException em APIs mais recentes.
            File imagePath = new File(getFilesDir(), "my_images"); // Diretório interno
            if (!imagePath.exists()) imagePath.mkdirs();
            tempFile = new File(imagePath, "ocr_temp_image.jpg");
            tempImageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider", // Deve corresponder ao authorities no Manifest
                    tempFile);
            takePictureLauncher.launch(tempImageUri);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar arquivo temporário para câmera: ", e);
            Toast.makeText(this, "Erro ao preparar câmera.", Toast.LENGTH_SHORT).show();
            tempImageUri = null; // Reseta se falhar
        }
    }


    private void performOcr(Uri imageUri) {
        textViewOcrResult.setText("Processando OCR...");
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        Log.i(TAG, "OCR bem-sucedido.");
                        textViewOcrResult.setText(visionText.getText());
                        if (visionText.getText().isEmpty()){
                            Toast.makeText(OcrActivity.this, "Nenhum texto encontrado.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Falha no OCR: ", e);
                        textViewOcrResult.setText("Falha no OCR: " + e.getMessage());
                        Toast.makeText(OcrActivity.this, "Falha ao processar texto.", Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            Log.e(TAG, "Erro ao criar InputImage: ", e);
            textViewOcrResult.setText("Erro ao carregar imagem para OCR.");
            Toast.makeText(this, "Erro ao carregar imagem.", Toast.LENGTH_SHORT).show();
        }
    }

    private void performOcr(Bitmap bitmap) {
        textViewOcrResult.setText("Processando OCR...");
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    Log.i(TAG, "OCR (Bitmap) bem-sucedido.");
                    textViewOcrResult.setText(visionText.getText());
                    if (visionText.getText().isEmpty()){
                        Toast.makeText(OcrActivity.this, "Nenhum texto encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Falha no OCR (Bitmap): ", e);
                    textViewOcrResult.setText("Falha no OCR (Bitmap): " + e.getMessage());
                    Toast.makeText(OcrActivity.this, "Falha ao processar texto da imagem do PDF.", Toast.LENGTH_SHORT).show();
                });
    }


    private void convertPdfPageToImageAndPerformOcr(Uri pdfUri, int pageNumber) {
        textViewOcrResult.setText("Convertendo PDF para imagem...");
        ParcelFileDescriptor parcelFileDescriptor = null;
        PdfRenderer pdfRenderer = null;
        PdfRenderer.Page currentPage = null;
        Bitmap bitmap = null;

        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
            if (parcelFileDescriptor != null) {
                pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                if (pdfRenderer.getPageCount() > pageNumber) {
                    currentPage = pdfRenderer.openPage(pageNumber);

                    // Cria um bitmap com fundo branco para melhor OCR em alguns casos
                    bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE); // Fundo branco
                    //canvas.drawBitmap(bitmap, 0, 0, null); // Desnecessário aqui, já que o bitmap é novo

                    currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    imageViewPreview.setImageBitmap(bitmap); // Mostra a página do PDF renderizada
                    performOcr(bitmap); // Realiza o OCR no bitmap da página
                } else {
                    Toast.makeText(this, "Número da página inválido ou PDF sem páginas.", Toast.LENGTH_LONG).show();
                    textViewOcrResult.setText("Não foi possível renderizar a página do PDF.");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao renderizar PDF: ", e);
            Toast.makeText(this, "Erro ao abrir ou renderizar PDF.", Toast.LENGTH_LONG).show();
            textViewOcrResult.setText("Erro ao processar PDF.");
        } finally {
            try {
                if (currentPage != null) {
                    currentPage.close();
                }
                if (pdfRenderer != null) {
                    pdfRenderer.close();
                }
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
                // Não recicle o bitmap aqui se ele ainda estiver sendo usado pelo ImageView ou OCR.
                // O garbage collector cuidará disso. Se você precisar reciclar explicitamente,
                // faça-o quando tiver certeza que não é mais necessário.
            } catch (IOException e) {
                Log.e(TAG, "Erro ao fechar recursos do PDF: ", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textRecognizer != null) {
            // textRecognizer.close(); // ML Kit geralmente gerencia seu próprio ciclo de vida,
            // mas se houver um método close() explícito na versão que você usar, chame-o.
            // Para play-services-mlkit-text-recognition, não é necessário fechar explicitamente.
        }
    }
}
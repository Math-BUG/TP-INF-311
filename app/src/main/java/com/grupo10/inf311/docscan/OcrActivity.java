package com.grupo10.inf311.docscan;

import android.Manifest;
import android.content.Intent; // IMPORT NECESSÁRIO
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
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

    // NOVA CONSTANTE: Chave pública para o Intent extra que carrega a URI da imagem.
    // Isso permite que outras atividades (como DocumentActionActivity) enviem dados para esta.
    public static final String EXTRA_IMAGE_URI = "com.grupo10.inf311.docscan.EXTRA_IMAGE_URI";

    private Button buttonCaptureImage, buttonSelectImage, buttonSelectPdf;
    private ImageView imageViewPreview;
    private TextView textViewOcrResult;
    private TextRecognizer textRecognizer;

    private Uri tempImageUri;

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
                if (result && tempImageUri != null) {
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
                    imageViewPreview.setImageResource(R.drawable.edittext_border_background);
                    convertPdfPageToImageAndPerformOcr(uri, 0);
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

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // LÓGICA ADICIONADA: Verifica se a atividade foi iniciada com uma imagem vinda de outra tela.
        handleIncomingIntent();

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

    /**
     * NOVO MÉTODO
     * Verifica se a atividade recebeu um caminho de imagem de outra atividade.
     * Se sim, exibe a imagem e inicia o OCR automaticamente.
     */
    private void handleIncomingIntent() {
        Intent intent = getIntent();
        // Verifica se o Intent não é nulo e se ele contém o nosso "extra"
        if (intent != null && intent.hasExtra(EXTRA_IMAGE_URI)) {
            String imagePath = intent.getStringExtra(EXTRA_IMAGE_URI);

            // Verifica se o caminho da imagem é válido
            if (imagePath != null && !imagePath.isEmpty()) {
                // Converte a String do caminho para um objeto Uri
                Uri imageUri = Uri.parse(imagePath);

                // Exibe a imagem no ImageView
                imageViewPreview.setImageURI(imageUri);
                // Inicia o processo de OCR para esta imagem
                performOcr(imageUri);

                // Para uma melhor experiência do usuário, esconde os botões de seleção,
                // já que a imagem foi fornecida automaticamente.
                buttonCaptureImage.setVisibility(View.GONE);
                buttonSelectImage.setVisibility(View.GONE);
                buttonSelectPdf.setVisibility(View.GONE);
            }
        }
    }


    private void launchCamera() {
        File tempFile;
        try {
            File imagePath = new File(getFilesDir(), "my_images");
            if (!imagePath.exists()) imagePath.mkdirs();
            tempFile = new File(imagePath, "ocr_temp_image.jpg");
            tempImageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    tempFile);
            takePictureLauncher.launch(tempImageUri);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar arquivo temporário para câmera: ", e);
            Toast.makeText(this, "Erro ao preparar câmera.", Toast.LENGTH_SHORT).show();
            tempImageUri = null;
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
                        Toast.makeText(this, "Falha ao processar texto.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Falha ao processar texto da imagem do PDF.", Toast.LENGTH_SHORT).show();
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

                    bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);

                    currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    imageViewPreview.setImageBitmap(bitmap);
                    performOcr(bitmap);
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
            } catch (IOException e) {
                Log.e(TAG, "Erro ao fechar recursos do PDF: ", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberação de recursos, se necessário
    }
}
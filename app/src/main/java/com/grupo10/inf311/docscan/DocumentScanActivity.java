package com.grupo10.inf311.docscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class DocumentScanActivity extends AppCompatActivity {
    private static final String TAG = "DocumentScanActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private PreviewView previewView;
    private DocumentScanOverlayView overlayView;
    private Button btnCapture;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_scan);

        initializeViews();
        setupListeners();

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.scanOverlay);
        btnCapture = findViewById(R.id.btnCapture);
    }

    private void setupListeners() {
        btnCapture.setOnClickListener(v -> capturePhoto());

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissão de câmera necessária", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erro ao inicializar câmera", e);
                Toast.makeText(this, "Erro ao inicializar câmera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao vincular preview da câmera", e);
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Câmera não está pronta", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = createImageFile();
        if (photoFile == null) {
            Toast.makeText(this, "Erro ao criar arquivo de imagem", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(photoFile)
                .build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Log.d(TAG, "Imagem capturada: " + currentPhotoPath);
                        processAndCropImage(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Erro ao capturar imagem", exception);
                        Toast.makeText(DocumentScanActivity.this,
                                "Erro ao capturar imagem", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String imageFileName = "RG_" + timeStamp;
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            Log.e(TAG, "Erro ao criar arquivo de imagem", e);
            return null;
        }
    }

    private void processAndCropImage(File imageFile) {
        try {
            // Carregar a imagem original
            Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (originalBitmap == null) {
                Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
                return;
            }

            // Corrigir orientação da imagem
            Bitmap rotatedBitmap = rotateImageIfRequired(originalBitmap, imageFile.getAbsolutePath());

            // Obter dimensões da imagem e do preview
            int imageWidth = rotatedBitmap.getWidth();
            int imageHeight = rotatedBitmap.getHeight();
            int previewWidth = previewView.getWidth();
            int previewHeight = previewView.getHeight();

            // Obter retângulo do overlay
            RectF documentRect = overlayView.getDocumentRect();

            // Calcular coordenadas de corte
            float scaleX = (float) imageWidth / previewWidth;
            float scaleY = (float) imageHeight / previewHeight;

            int cropX = Math.max(0, (int) (documentRect.left * scaleX));
            int cropY = Math.max(0, (int) (documentRect.top * scaleY));
            int cropWidth = Math.min((int) (documentRect.width() * scaleX), imageWidth - cropX);
            int cropHeight = Math.min((int) (documentRect.height() * scaleY), imageHeight - cropY);

            // Recortar a imagem
            Bitmap croppedBitmap = Bitmap.createBitmap(rotatedBitmap, cropX, cropY, cropWidth, cropHeight);

            // Salvar a imagem recortada
            saveProcessedImage(croppedBitmap, imageFile);

            // Liberar memória
            if (originalBitmap != rotatedBitmap) {
                originalBitmap.recycle();
            }
            rotatedBitmap.recycle();
            croppedBitmap.recycle();

            // Processar o RG
            processRgDocument(imageFile);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar imagem", e);
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler EXIF", e);
            return bitmap;
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void saveProcessedImage(Bitmap bitmap, File file) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Log.d(TAG, "Imagem processada salva: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar imagem processada", e);
        }
    }

    private void processRgDocument(File imageFile) {
        Intent intent = new Intent(this, RgProcessingActivity.class);
        Uri imageUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider", imageFile);

        intent.putExtra(RgProcessingActivity.EXTRA_IMAGE_PATH, imageFile.getAbsolutePath());
        startActivityForResult(intent, 1001);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
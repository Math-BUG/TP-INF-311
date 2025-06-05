// HomeActivity.java
package com.grupo10.inf311.docscan;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements DocumentAdapter.OnDocumentClickListener {

    private ArrayList<Document> docList = new ArrayList<>();
    private DocumentAdapter adapter;
    private RecyclerView recyclerView;
    private String currentPhotoPath;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;

    // Constantes para identificar qual ação de origem de imagem foi selecionada
    private static final int SOURCE_CAMERA = 0;
    private static final int SOURCE_GALLERY = 1;
    private int selectedImageSource = -1; // Para saber qual ação o usuário quer depois de permissões

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupActivityResultLaunchers();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
      /*  bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    return true;
                case R.id.menu_scan:
                    startActivity(new Intent(this, OcrActivity.class));
                    return true;
                case R.id.menu_settings:
                    startActivity(new Intent(this, tela_cadastro.class));
                    return true;
            }
            return false;
        });*/

        recyclerView = findViewById(R.id.recyclerViewDocuments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocumentAdapter(docList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.buttonAdd);
        fabAdd.setOnClickListener(view -> showAddDocumentOptions());

        docList.add(new Document(UUID.randomUUID().toString(), "Relatório de Vendas", "01/01/2025", ""));
        docList.add(new Document(UUID.randomUUID().toString(), "Contrato XYZ", "15/02/2025", ""));
        adapter.notifyDataSetChanged();
    }

    private void setupActivityResultLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        addNewDocument(currentPhotoPath);
                    } else {
                        Toast.makeText(this, "Foto não tirada ou cancelada.", Toast.LENGTH_SHORT).show();
                        currentPhotoPath = null;
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        addNewDocument(uri.toString());
                    } else {
                        Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show();
                    }
                });

        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    // Verificar se todas as permissões essenciais para a ação escolhida foram concedidas
                    boolean allGranted = true;
                    if (selectedImageSource == SOURCE_CAMERA) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
                            // Em Q+, se estamos salvando em externalFilesDir, CAMERA é a principal
                            allGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);
                        } else { // Android 9 e abaixo
                            allGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false) &&
                                    permissions.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
                        }
                    } else if (selectedImageSource == SOURCE_GALLERY) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
                            allGranted = permissions.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6-12 (READ_EXTERNAL_STORAGE é importante)
                            // Para GetContent(), READ_EXTERNAL_STORAGE não é estritamente necessário para *selecionar* um item que o usuário *escolhe*.
                            // No entanto, se você tivesse outras lógicas que precisassem de acesso amplo, precisaria.
                            // Para o fluxo de GetContent, não há uma permissão de armazenamento explícita que você solicite e que seja *necessária* para o contrato em si.
                            // O Android gerencia o acesso ao URI retornado.
                            // Mas, para ser seguro, podemos verificar se o usuário *negou* o armazenamento.
                            // Simplificando, se 'GetContent' falhar, provavelmente foi um problema de acesso.
                            allGranted = true; // Por padrão, para GetContent(), assumimos que está ok se o usuário conseguir selecionar.
                        }
                    }

                    if (allGranted) {
                        // Permissões concedidas para a ação específica, prossiga
                        if (selectedImageSource == SOURCE_CAMERA) {
                            startCameraIntent();
                        } else if (selectedImageSource == SOURCE_GALLERY) {
                            startGalleryIntent();
                        }
                    } else {
                        // Permissões ainda não concedidas ou negadas persistentemente
                        Toast.makeText(HomeActivity.this, "Permissões essenciais foram negadas. Conceda-as nas configurações do aplicativo.", Toast.LENGTH_LONG).show();
                        showSettingsDialog();
                    }
                    selectedImageSource = -1; // Reset
                });
    }

    private void showAddDocumentOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Documento")
                .setItems(new CharSequence[]{"Tirar Foto", "Escolher da Galeria"}, (dialog, which) -> {
                    if (which == 0) { // Tirar Foto
                        selectedImageSource = SOURCE_CAMERA;
                        checkAndRequestPermissionsForCamera();
                    } else { // Escolher da Galeria
                        selectedImageSource = SOURCE_GALLERY;
                        checkAndRequestPermissionsForGallery();
                    }
                })
                .show();
    }

    private void checkAndRequestPermissionsForCamera() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }
        // WRITE_EXTERNAL_STORAGE é necessária para salvar imagens em Android 9 e abaixo
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            // Permissões já concedidas, inicie a câmera
            startCameraIntent();
        }
    }

    private void checkAndRequestPermissionsForGallery() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6-12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        // Para Android 5 (Lollipop) e abaixo, as permissões são concedidas em tempo de instalação.

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            // Permissões já concedidas ou não necessárias para esta versão, inicie a galeria
            startGalleryIntent();
        }
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Erro ao criar arquivo de imagem: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile);
                takePictureLauncher.launch(photoURI);
            }
        } else {
            Toast.makeText(this, "Nenhum aplicativo de câmera encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startGalleryIntent() {
        // Contentção é a melhor forma de interagir com a galeria a partir de Android 4.4
        // Não requer READ_EXTERNAL_STORAGE se o usuário escolher o arquivo através do picker.
        pickImageLauncher.launch("image/*");
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Usa getExternalFilesDir() que não precisa de permissão de armazenamento explícita em Q+
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            throw new IOException("Diretório de armazenamento externo não disponível.");
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void addNewDocument(String imagePath) {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String docName = "Scan " + currentDate;
        String documentId = UUID.randomUUID().toString();
        adapter.addDocument(0, new Document(documentId, docName, "Today", imagePath));
        recyclerView.smoothScrollToPosition(0);
        Toast.makeText(this, "Documento adicionado!", Toast.LENGTH_SHORT).show();
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissões Necessárias")
                .setMessage("As permissões de câmera e/ou armazenamento são essenciais para esta funcionalidade. Por favor, habilite-as nas configurações do aplicativo.")
                .setPositiveButton("Abrir Configurações", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDocumentClick(String documentId, boolean isSelected) {
        // Lógica de seleção
    }

    @Override
    public void onDocumentAction(String documentId) {
        Document docToAct = null;
        for (Document doc : docList) {
            if (doc.getId().equals(documentId)) {
                docToAct = doc;
                break;
            }
        }

        if (docToAct != null) {
            Intent intent = new Intent(this, DocumentActionActivity.class);
            intent.putExtra(DocumentActionActivity.EXTRA_DOCUMENT_ID, docToAct.getId());
            intent.putExtra(DocumentActionActivity.EXTRA_DOCUMENT_NAME, docToAct.getName());
            intent.putExtra(DocumentActionActivity.EXTRA_DOCUMENT_IMAGE_PATH, docToAct.getImagePath());
            startActivity(intent);
        }
    }
}
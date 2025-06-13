package com.grupo10.inf311.docscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    // Constantes para Intents
    public static final String EXTRA_NEW_DOC_ID = "EXTRA_NEW_DOC_ID";
    public static final String EXTRA_NEW_DOC_NAME = "EXTRA_NEW_DOC_NAME";
    public static final String EXTRA_NEW_DOC_DATE = "EXTRA_NEW_DOC_DATE";
    public static final String EXTRA_NEW_DOC_PATH = "EXTRA_NEW_DOC_PATH";

    // Views e Adapters
    private ArrayList<Document> docList = new ArrayList<>();
    private DocumentAdapter adapter;
    private RecyclerView recyclerView;

    // Componentes da Barra de Navegação Customizada
    private LinearLayout navHomeLayout;
    private View navHomeUnderline;
    private Button navScanButton;
    private LinearLayout navSettingsLayout;
    private View navSettingsUnderline;

    // Lógica de Captura de Imagem
    private String currentPhotoPath;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private static final int SOURCE_CAMERA = 0;
    private static final int SOURCE_GALLERY = 1;
    private int selectedImageSource = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        setupActivityResultLaunchers();
        setupListeners();

        if (savedInstanceState == null) {
            loadSampleDocuments();
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewDocuments);
        navHomeLayout = findViewById(R.id.nav_home_item_layout);
        navScanButton = findViewById(R.id.nav_scan_button);
        navSettingsLayout = findViewById(R.id.nav_settings_item_layout);
        navHomeUnderline = findViewById(R.id.nav_home_underline);
        navSettingsUnderline = findViewById(R.id.nav_settings_underline);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocumentAdapter(docList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        FloatingActionButton fabAdd = findViewById(R.id.buttonAdd);
        fabAdd.setOnClickListener(view -> showAddDocumentOptions());

        navHomeLayout.setOnClickListener(v -> {
            selectNavItem(navHomeUnderline, navSettingsUnderline);
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        });

        navScanButton.setOnClickListener(v -> {
            Toast.makeText(this, "SCAN", Toast.LENGTH_SHORT).show();
            showAddDocumentOptions();
        });

        navSettingsLayout.setOnClickListener(v -> {
            selectNavItem(navSettingsUnderline, navHomeUnderline);
            // Abre o BottomSheetFragment para as configurações
            SettingsBottomSheetFragment bottomSheet = new SettingsBottomSheetFragment();
            bottomSheet.show(getSupportFragmentManager(), SettingsBottomSheetFragment.TAG);
        });
    }

    private void selectNavItem(View activeUnderline, View inactiveUnderline) {
        activeUnderline.setVisibility(View.VISIBLE);
        inactiveUnderline.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.hasExtra(EXTRA_NEW_DOC_ID)) {
            String id = intent.getStringExtra(EXTRA_NEW_DOC_ID);
            String name = intent.getStringExtra(EXTRA_NEW_DOC_NAME);
            String date = intent.getStringExtra(EXTRA_NEW_DOC_DATE);
            String path = intent.getStringExtra(EXTRA_NEW_DOC_PATH);

            if (id != null && name != null && date != null && path != null) {
                adapter.addDocument(0, new Document(id, name, date, path));
                recyclerView.smoothScrollToPosition(0);
                Toast.makeText(this, "Novo documento salvo!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSampleDocuments() {
        docList.add(new Document(UUID.randomUUID().toString(), "Relatório de Vendas (Exemplo)", "01/01/2025", ""));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDocumentClick(String documentId, boolean isSelected) {
        // Lógica para feedback visual da seleção
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

    // ===============================================================
    // LÓGICA DE CAPTURA DE IMAGEM E PERMISSÕES (sem alterações)
    // ===============================================================
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
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        addNewDocument(uri.toString());
                    } else {
                        Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show();
                    }
                });

        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    if (selectedImageSource == SOURCE_CAMERA) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            allGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);
                        } else {
                            allGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false) &&
                                    permissions.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
                        }
                    } else if (selectedImageSource == SOURCE_GALLERY) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            allGranted = permissions.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                        } else {
                            allGranted = true;
                        }
                    }

                    if (allGranted) {
                        if (selectedImageSource == SOURCE_CAMERA) {
                            startCameraIntent();
                        } else if (selectedImageSource == SOURCE_GALLERY) {
                            startGalleryIntent();
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Permissões essenciais foram negadas.", Toast.LENGTH_LONG).show();
                        showSettingsDialog();
                    }
                    selectedImageSource = -1;
                });
    }

    private void showAddDocumentOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Documento")
                .setItems(new CharSequence[]{"Tirar Foto", "Escolher da Galeria"}, (dialog, which) -> {
                    if (which == 0) {
                        selectedImageSource = SOURCE_CAMERA;
                        checkAndRequestPermissionsForCamera();
                    } else {
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
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            startCameraIntent();
        }
    }

    private void checkAndRequestPermissionsForGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionsLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
                return;
            }
        }
        startGalleryIntent();
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Erro ao criar arquivo de imagem.", Toast.LENGTH_LONG).show();
            return;
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    photoFile);
            takePictureLauncher.launch(photoURI);
        }
    }

    private void startGalleryIntent() {
        pickImageLauncher.launch("image/*");
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void addNewDocument(String imagePath) {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String docName = "Imagem Scan " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String documentId = UUID.randomUUID().toString();
        adapter.addDocument(0, new Document(documentId, docName, currentDate, imagePath));
        recyclerView.smoothScrollToPosition(0);
        Toast.makeText(this, "Imagem adicionada!", Toast.LENGTH_SHORT).show();
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissões Necessárias")
                .setMessage("As permissões são essenciais. Habilite-as nas configurações.")
                .setPositiveButton("Configurações", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

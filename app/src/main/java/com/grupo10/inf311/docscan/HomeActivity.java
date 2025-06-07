package com.grupo10.inf311.docscan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements DocumentAdapter.OnDocumentClickListener {

    // Chaves para receber os dados do novo documento
    public static final String EXTRA_NEW_DOC_ID = "EXTRA_NEW_DOC_ID";
    public static final String EXTRA_NEW_DOC_NAME = "EXTRA_NEW_DOC_NAME";
    public static final String EXTRA_NEW_DOC_DATE = "EXTRA_NEW_DOC_DATE";
    public static final String EXTRA_NEW_DOC_PATH = "EXTRA_NEW_DOC_PATH";

    private ArrayList<Document> docList = new ArrayList<>();
    private DocumentAdapter adapter;
    private RecyclerView recyclerView;
    private String currentPhotoPath;

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    addNewDocument(currentPhotoPath);
                } else {
                    Toast.makeText(this, "Foto não tirada ou cancelada.", Toast.LENGTH_SHORT).show();
                    currentPhotoPath = null;
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    addNewDocument(uri.toString());
                } else {
                    Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerViewDocuments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocumentAdapter(docList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.buttonAdd);
        fabAdd.setOnClickListener(view -> showAddDocumentOptions());

        docList.add(new Document(UUID.randomUUID().toString(), "Relatório de Vendas", "01/01/2025", ""));
        docList.add(new Document(UUID.randomUUID().toString(), "Contrato XYZ", "15/02/2025", ""));
        adapter.notifyDataSetChanged();

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_NEW_DOC_ID)) {
            String id = intent.getStringExtra(EXTRA_NEW_DOC_ID);
            String name = intent.getStringExtra(EXTRA_NEW_DOC_NAME);
            String date = intent.getStringExtra(EXTRA_NEW_DOC_DATE);
            String path = intent.getStringExtra(EXTRA_NEW_DOC_PATH);

            if (id != null) {
                Document newDocument = new Document(id, name, date, path);
                docList.add(0, newDocument);
                adapter.notifyItemInserted(0);
                recyclerView.smoothScrollToPosition(0);
                Toast.makeText(this, "Novo documento de texto adicionado!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddDocumentOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Documento")
                .setItems(new CharSequence[]{"Tirar Foto", "Escolher da Galeria"}, (dialog, which) -> {
                    if (which == 0) {
                        startCameraIntent();
                    } else {
                        startGalleryIntent();
                    }
                })
                .show();
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
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
            currentPhotoPath = photoURI.toString();
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
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void addNewDocument(String imagePath) {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String docName = "Scan " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String documentId = UUID.randomUUID().toString();
        Document newDoc = new Document(documentId, docName, currentDate, imagePath);
        docList.add(0, newDoc);
        adapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
        Toast.makeText(this, "Documento adicionado!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDocumentClick(String documentId, boolean isSelected) {
        // Lógica para seleção visual
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
            if (TextUtils.isEmpty(docToAct.getImagePath())) {
                Toast.makeText(this, "Este documento não possui uma imagem para processar.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, DocumentActionActivity.class);
            intent.putExtra(DocumentActionActivity.EXTRA_DOCUMENT_ID, docToAct.getId());
            intent.putExtra(DocumentActionActivity.EXTRA_DOCUMENT_NAME, docToAct.getName());
            intent.putExtra(DocumentActionActivity.EXTRA_DOCUMENT_IMAGE_PATH, docToAct.getImagePath());
            startActivity(intent);
        }
    }
}
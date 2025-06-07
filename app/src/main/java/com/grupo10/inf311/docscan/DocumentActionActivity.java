package com.grupo10.inf311.docscan;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;

public class DocumentActionActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "EXTRA_DOCUMENT_ID";
    public static final String EXTRA_DOCUMENT_NAME = "EXTRA_DOCUMENT_NAME";
    public static final String EXTRA_DOCUMENT_IMAGE_PATH = "EXTRA_DOCUMENT_IMAGE_PATH";

    private String documentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_action);

        TextView tvDocumentId = findViewById(R.id.tvDocumentId);
        TextView tvDocumentName = findViewById(R.id.tvDocumentName);
        ImageView imgFullDocument = findViewById(R.id.imgFullDocument);
        Button btnAction = findViewById(R.id.btnAction);
        Button btnDocScan = findViewById(R.id.btnDocScan);

        Intent intent = getIntent();
        String documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID);
        String documentName = intent.getStringExtra(EXTRA_DOCUMENT_NAME);
        documentImagePath = intent.getStringExtra(EXTRA_DOCUMENT_IMAGE_PATH);

        tvDocumentId.setText("Document ID: " + documentId);
        tvDocumentName.setText("Document Name: " + documentName);

        if (documentImagePath != null && !documentImagePath.isEmpty()) {
            Glide.with(this).load(Uri.parse(documentImagePath)).into(imgFullDocument);
        } else {
            imgFullDocument.setImageResource(R.drawable.ic_launcher_background);
        }

        // --- LÓGICA DO BOTÃO "ACTION" ATUALIZADA ---
        // Agora o botão abre um diálogo com opções.
        btnAction.setOnClickListener(v -> {
            showActionDialog();
        });

        btnDocScan.setOnClickListener(v -> {
            // Apenas imagens podem ser processadas pelo OCR
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(documentImagePath);
            if (TextUtils.isEmpty(documentImagePath) || "txt".equalsIgnoreCase(fileExtension)) {
                Toast.makeText(this, "Apenas imagens podem passar pelo processo de OCR.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent processingIntent = new Intent(this, ProcessingActivity.class);
            processingIntent.putExtra(OcrActivity.EXTRA_IMAGE_URI, documentImagePath);
            startActivity(processingIntent);
        });
    }

    /**
     * NOVO MÉTODO: Cria e exibe um diálogo com a lista de ações para o documento.
     */
    private void showActionDialog() {
        final CharSequence[] options = {"Visualizar Documento", "Compartilhar", "Renomear", "Excluir"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha uma Ação");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Visualizar Documento")) {
                viewDocument(); // Reutiliza o método que já criamos
            } else if (options[item].equals("Compartilhar")) {
                shareDocument(); // Chama um novo método para compartilhar
            } else if (options[item].equals("Renomear")) {
                Toast.makeText(this, "Função Renomear a ser implementada.", Toast.LENGTH_SHORT).show();
            } else if (options[item].equals("Excluir")) {
                Toast.makeText(this, "Função Excluir a ser implementada.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    /**
     * NOVO MÉTODO: Compartilha o arquivo do documento.
     */
    private void shareDocument() {
        if (TextUtils.isEmpty(documentImagePath)) {
            Toast.makeText(this, "Nenhum arquivo de documento para compartilhar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri pathUri = Uri.parse(documentImagePath);
        File fileToShare = new File(pathUri.getPath());

        if (!fileToShare.exists()) {
            Toast.makeText(this, "Arquivo não encontrado para compartilhar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri contentUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                fileToShare
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(getMimeType(contentUri.toString()));
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Compartilhar documento via..."));
    }

    /**
     * Este método agora é uma das opções do diálogo.
     */
    private void viewDocument() {
        if (TextUtils.isEmpty(documentImagePath)) {
            Toast.makeText(this, "Nenhum arquivo de documento para visualizar.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(documentImagePath);

        if ("txt".equalsIgnoreCase(fileExtension)) {
            Intent textViewerIntent = new Intent(this, TextViewerActivity.class);
            textViewerIntent.putExtra(TextViewerActivity.EXTRA_FILE_PATH, documentImagePath);
            startActivity(textViewerIntent);
        } else {
            openFileWithSystemViewer(Uri.parse(documentImagePath));
        }
    }

    private void openFileWithSystemViewer(Uri pathUri) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(pathUri, getMimeType(pathUri.toString()));
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(viewIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nenhum aplicativo encontrado para abrir este arquivo.", Toast.LENGTH_LONG).show();
        }
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }
}
// DocumentActionActivity.java
package com.grupo10.inf311.docscan;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Importe esta classe
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DocumentActionActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "document_id";
    public static final String EXTRA_DOCUMENT_NAME = "document_name";
    public static final String EXTRA_DOCUMENT_IMAGE_PATH = "document_image_path";

    // Variáveis para armazenar os dados do documento recebido
    private String documentId;
    private String documentName;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_action);

        TextView tvDocumentId = findViewById(R.id.tvDocumentId);
        TextView tvDocumentName = findViewById(R.id.tvDocumentName);
        ImageView imgFullDocument = findViewById(R.id.imgFullDocument);
        Button btnAction = findViewById(R.id.btnAction);
        Button btnDocScan = findViewById(R.id.btnDocScan);

        // Lógica do Intent para receber os dados
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_DOCUMENT_ID)) {
            // Armazena os dados nas variáveis da classe
            this.documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID);
            this.documentName = intent.getStringExtra(EXTRA_DOCUMENT_NAME);
            this.imagePath = intent.getStringExtra(EXTRA_DOCUMENT_IMAGE_PATH);

            tvDocumentName.setText("Nome: " + this.documentName);
            tvDocumentId.setText("ID: " + this.documentId); // Exibindo o ID também

            if (this.imagePath != null && !this.imagePath.isEmpty()) {
                Glide.with(this).load(this.imagePath).into(imgFullDocument);
            } else {
                imgFullDocument.setImageResource(R.drawable.menu36); // Imagem placeholder
            }
        } else {
            Toast.makeText(this, "Nenhum documento selecionado.", Toast.LENGTH_SHORT).show();
            finish();
            return; // Encerra o onCreate se não houver dados
        }

        // --- LISTENER DO BOTÃO DE OCR CORRIGIDO ---
        btnDocScan.setOnClickListener(v -> {
            // Verifica se há um caminho de imagem antes de iniciar
            if (TextUtils.isEmpty(this.imagePath)) {
                Toast.makeText(this, "Este documento não possui uma imagem para processar.", Toast.LENGTH_SHORT).show();
                return;
            }

            // MUDANÇA AQUI: Inicia a ProcessingActivity em vez da OcrActivity
            Intent processingIntent = new Intent(this, ProcessingActivity.class);

            // Envia o caminho da imagem para a ProcessingActivity
            processingIntent.putExtra(OcrActivity.EXTRA_IMAGE_URI, this.imagePath); // Podemos reusar a mesma chave

            // Inicia a nova tela
            startActivity(processingIntent);
        });

        // Listener do outro botão (ação indefinida)
        btnAction.setOnClickListener(v -> {
            // ATENÇÃO: LanguageToolResponse não é uma Activity e não pode ser iniciada com um Intent.
            // A linha abaixo causará um erro.
            // Intent it = new Intent(this, LanguageToolResponse.class);
            // startActivity(it);
            Toast.makeText(this, "Ação para este botão precisa ser definida.", Toast.LENGTH_SHORT).show();
        });

        // Lógica do Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_scan) {
                // Inicia a OcrActivity para um novo scan, sem imagem pré-definida
                startActivity(new Intent(getApplicationContext(), OcrActivity.class));
                return true;
            } else if (itemId == R.id.menu_home) {
                // Volta para a lista de documentos
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                return true;
            } else if (itemId == R.id.menu_settings) {
                Toast.makeText(DocumentActionActivity.this, "Configurações clicado!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}
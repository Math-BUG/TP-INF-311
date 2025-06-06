// DocumentActionActivity.java
package com.grupo10.inf311.docscan; // Verifique o seu pacote

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DocumentActionActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "document_id";
    public static final String EXTRA_DOCUMENT_NAME = "document_name";
    public static final String EXTRA_DOCUMENT_IMAGE_PATH = "document_image_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_action);

        TextView tvDocumentId = findViewById(R.id.tvDocumentId);
        TextView tvDocumentName = findViewById(R.id.tvDocumentName);
        ImageView imgFullDocument = findViewById(R.id.imgFullDocument);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnShare = findViewById(R.id.btnShare);


        // Receber os dados do Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_DOCUMENT_ID)) {
            String documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID);
            String documentName = intent.getStringExtra(EXTRA_DOCUMENT_NAME);
            String imagePath = intent.getStringExtra(EXTRA_DOCUMENT_IMAGE_PATH);

            //tvDocumentId.setText("ID: " + documentId);
            tvDocumentName.setText("Nome: " + documentName);

            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(this)
                        .load(imagePath)
                        .into(imgFullDocument);
            } else {
                imgFullDocument.setImageResource(R.drawable.menu36); // Ícone padrão
            }
        } else {
            Toast.makeText(this, "Nenhum documento selecionado.", Toast.LENGTH_SHORT).show();
            finish(); // Fecha a Activity se não houver dados
        }

        // Configurar Listeners para os botões de ação
        btnEdit.setOnClickListener(v -> Toast.makeText(this, "Editar clicado!", Toast.LENGTH_SHORT).show());
        btnShare.setOnClickListener(v -> Toast.makeText(this, "Compartilhar clicado!", Toast.LENGTH_SHORT).show());

    }
}
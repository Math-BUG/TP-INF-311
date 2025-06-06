// DocumentActionActivity.java
package com.grupo10.inf311.docscan; // Verifique o seu pacote

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView; // 1. IMPORT ADICIONADO

public class DocumentActionActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "document_id";
    public static final String EXTRA_DOCUMENT_NAME = "document_name";
    public static final String EXTRA_DOCUMENT_IMAGE_PATH = "document_image_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_action);

        // Seus componentes existentes
        TextView tvDocumentId = findViewById(R.id.tvDocumentId);
        TextView tvDocumentName = findViewById(R.id.tvDocumentName);
        ImageView imgFullDocument = findViewById(R.id.imgFullDocument);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnShare = findViewById(R.id.btnShare);

        // Lógica do Intent (permanece a mesma)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_DOCUMENT_ID)) {
            String documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID);
            String documentName = intent.getStringExtra(EXTRA_DOCUMENT_NAME);
            String imagePath = intent.getStringExtra(EXTRA_DOCUMENT_IMAGE_PATH);
            tvDocumentName.setText("Nome: " + documentName);

            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(this).load(imagePath).into(imgFullDocument);
            } else {
                imgFullDocument.setImageResource(R.drawable.menu36);
            }
        } else {
            Toast.makeText(this, "Nenhum documento selecionado.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Listeners dos botões (permanecem os mesmos)
        btnEdit.setOnClickListener(v -> Toast.makeText(this, "Editar clicado!", Toast.LENGTH_SHORT).show());
        btnShare.setOnClickListener(v -> Toast.makeText(this, "Compartilhar clicado!", Toast.LENGTH_SHORT).show());


        // 2. LÓGICA DO BOTTOM NAVIGATION ADICIONADA
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Opcional: define um item como selecionado (ex: o "home")
        bottomNavigationView.setSelectedItemId(R.id.menu_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_scan) {
                Toast.makeText(DocumentActionActivity.this, "Scan clicado!", Toast.LENGTH_SHORT).show();
                // Exemplo: startActivity(new Intent(getApplicationContext(), ScanActivity.class));
                return true;
            } else if (itemId == R.id.menu_home) {
                Toast.makeText(DocumentActionActivity.this, "Home clicado!", Toast.LENGTH_SHORT).show();
                // Exemplo: startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return true;
            } else if (itemId == R.id.menu_settings) {
                Toast.makeText(DocumentActionActivity.this, "Configurações clicado!", Toast.LENGTH_SHORT).show();
                // Exemplo: startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}
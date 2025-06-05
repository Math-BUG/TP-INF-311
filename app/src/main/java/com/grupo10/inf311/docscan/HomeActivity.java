package com.grupo10.inf311.docscan;
import com.grupo10.inf311.docscan.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<Document> docList = new ArrayList<>();
    private DocumentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializa o BottomNavigationView e configura o listener
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
           /* switch (item.getItemId()) {
                case R.id.menu_home:
                    // Já está na Home
                    return true;

                case R.id.menu_scan:
                    startActivity(new Intent(this, OcrActivity.class));
                    return true;

                case R.id.menu_settings:
                    startActivity(new Intent(this, tela_cadastro.class));
                    return true;
            }*/
            return false;
        });

        // Configura o RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDocuments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocumentAdapter(docList);
        recyclerView.setAdapter(adapter);

        // Configura o FloatingActionButton para adicionar documentos
        FloatingActionButton fabAdd = findViewById(R.id.buttonAdd);
        fabAdd.setOnClickListener(view -> addNewDocument());
    }

    private void addNewDocument() {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String docName = "Scan " + currentDate;
        docList.add(0, new Document(docName, "Today"));
        adapter.notifyItemInserted(0);
    }
}

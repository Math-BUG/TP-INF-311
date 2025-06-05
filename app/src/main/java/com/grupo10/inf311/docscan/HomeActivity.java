package com.grupo10.inf311.docscan;
// HomeActivity.java (sem grandes mudanças)
// HomeActivity.java
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements DocumentAdapter.OnDocumentClickListener {

    private ArrayList<Document> docList = new ArrayList<>();
    private DocumentAdapter adapter;
    private RecyclerView recyclerView; // Adicione esta referência

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // ... (seu código existente para BottomNavigationView)
            return false;
        });

        recyclerView = findViewById(R.id.recyclerViewDocuments); // Inicialize a referência
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocumentAdapter(docList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.buttonAdd);
        fabAdd.setOnClickListener(view -> addNewDocument());

        // Adicionar alguns documentos para teste
        addNewDocument();
        addNewDocument();
        addNewDocument();
    }

    private void addNewDocument() {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String docName = "Scan " + currentDate;
        String documentId = UUID.randomUUID().toString();
        adapter.addDocument(0, new Document(documentId, docName, "Today"));

        // *** AQUI É ONDE A MUDANÇA É FEITA ***
        // Rolamos o RecyclerView para a posição 0 (o item recém-adicionado)
        // Usar smoothScrollToPosition para uma animação suave, ou scrollToPosition para imediato
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onDocumentClick(String documentId, boolean isSelected) {
        if (isSelected) {
            Toast.makeText(this, "Documento Selecionado: " + documentId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Documento Desselecionado: " + documentId, Toast.LENGTH_SHORT).show();
        }

        String currentSelectedId = adapter.getSelectedDocumentId();
        if (currentSelectedId != null) {
            // Faça algo com o ID do documento selecionado
        }
    }

    public void doSomethingWithSelectedDocument() {
        String selectedDocId = adapter.getSelectedDocumentId();
        if (selectedDocId != null) {
            Toast.makeText(this, "Ação com o documento: " + selectedDocId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Nenhum documento selecionado para esta ação.", Toast.LENGTH_SHORT).show();
        }
    }
}
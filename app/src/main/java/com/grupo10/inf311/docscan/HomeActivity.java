package com.grupo10.inf311.docscan;
// HomeActivity.java (sem grandes mudanças)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // ... (seu código existente para BottomNavigationView)
            return false;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewDocuments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocumentAdapter(docList, this); // 'this' como listener
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
        // Use the adapter's method to add the document
        adapter.addDocument(0, new Document(documentId, docName, "Today"));
        // The adapter will handle notifyItemInserted and selectedPosition update
    }

    @Override
    public void onDocumentClick(String documentId, boolean isSelected) {
        // Agora, 'isSelected' será true apenas para o item que acabou de ser selecionado.
        // Se você clicar novamente no mesmo item, 'isSelected' será false (desselecionado).
        if (isSelected) {
            Toast.makeText(this, "Documento Selecionado: " + documentId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Documento Desselecionado: " + documentId, Toast.LENGTH_SHORT).show();
        }

        // Para obter o ID do documento atualmente selecionado a qualquer momento:
        String currentSelectedId = adapter.getSelectedDocumentId();
        if (currentSelectedId != null) {
            // Faça algo com o ID do documento selecionado
        }
    }

    // Exemplo de como obter o ID do documento selecionado em outro lugar na atividade
    public void doSomethingWithSelectedDocument() {
        String selectedDocId = adapter.getSelectedDocumentId();
        if (selectedDocId != null) {
            Toast.makeText(this, "Ação com o documento: " + selectedDocId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Nenhum documento selecionado para esta ação.", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.grupo10.inf311.docscan;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentActionActivity extends AppCompatActivity {

    private static final String TAG = "DocActionActivity";

    public static final String EXTRA_DOCUMENT_ID = "EXTRA_DOCUMENT_ID";
    public static final String EXTRA_DOCUMENT_NAME = "EXTRA_DOCUMENT_NAME";
    public static final String EXTRA_DOCUMENT_IMAGE_PATH = "EXTRA_DOCUMENT_IMAGE_PATH";

    private ImageView imgFullDocument;
    private TextView textViewActionResult;
    private String documentId;
    private String documentImagePath;
    private String ocrTextResult = "";


    private LanguageToolApiService languageToolService;

    private final ActivityResultLauncher<Intent> ocrResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra(ProcessingActivity.RESULT_OCR_TEXT)) {
                        ocrTextResult = data.getStringExtra(ProcessingActivity.RESULT_OCR_TEXT);
                        textViewActionResult.setText(ocrTextResult);
                        if (ocrTextResult == null || ocrTextResult.isEmpty()) {
                            Toast.makeText(this, "Nenhum texto foi encontrado na imagem.", Toast.LENGTH_LONG).show();
                        } else {
                            saveOcrTextToFile(ocrTextResult);
                        }
                    }
                } else {
                    Toast.makeText(this, "Processamento de OCR falhou ou foi cancelado.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_document_action);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Inicializa as Views
        imgFullDocument = findViewById(R.id.imgFullDocument);
        textViewActionResult = findViewById(R.id.textViewActionResult);
        TextView tvDocumentName = findViewById(R.id.tvDocumentName);
        Button btnAction = findViewById(R.id.btnAction);
        Button btnDocScan = findViewById(R.id.btnDocScan);
        ImageView buttonBack = findViewById(R.id.buttonBack);

        // Inicializa o serviço da LanguageTool API
        languageToolService = RetrofitClient.getApiService();

        // Pega os dados do Intent
        Intent intent = getIntent();
        documentId = intent.getStringExtra(EXTRA_DOCUMENT_ID);
        String documentName = intent.getStringExtra(EXTRA_DOCUMENT_NAME);
        documentImagePath = intent.getStringExtra(EXTRA_DOCUMENT_IMAGE_PATH);

        // Preenche os dados na tela
        tvDocumentName.setText(documentName != null ? documentName : "Detalhes do Documento");
        if (documentImagePath != null && !documentImagePath.isEmpty()) {
            Glide.with(this).load(Uri.parse(documentImagePath)).into(imgFullDocument);
        }

        // Configura os Listeners
        buttonBack.setOnClickListener(v -> finish());
        btnAction.setOnClickListener(v -> showActionDialog());
        btnDocScan.setOnClickListener(v -> {
            if (TextUtils.isEmpty(documentImagePath)) {
                Toast.makeText(this, "Nenhuma imagem para processar.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent processingIntent = new Intent(this, ProcessingActivity.class);
            processingIntent.putExtra(ProcessingActivity.EXTRA_IMAGE_URI, documentImagePath);
            ocrResultLauncher.launch(processingIntent);
        });

        loadOcrTextIfExists();
    }

    private void loadOcrTextIfExists() {
        if (documentId == null || documentId.isEmpty()) {
            return;
        }
        File textFile = new File(getFilesDir(), documentId + ".txt");
        if (textFile.exists()) {
            String savedText = readTextFromFile(textFile);
            if (!savedText.isEmpty()) {
                ocrTextResult = savedText;
                textViewActionResult.setText(savedText);
                Log.d(TAG, "Texto OCR carregado do arquivo: " + textFile.getName());
            }
        } else {
            textViewActionResult.setText("Clique em 'DocScan (OCR)' para extrair o texto da imagem.");
        }
    }

    private String readTextFromFile(File file) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler arquivo de texto salvo", e);
            return "";
        }
        return text.toString();
    }

    private void saveOcrTextToFile(String text) {
        if (documentId == null || documentId.isEmpty()) {
            Log.e(TAG, "Não é possível salvar o texto, ID do documento é nulo ou vazio.");
            return;
        }
        File file = new File(getFilesDir(), documentId + ".txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(text.getBytes());
            Log.d(TAG, "Texto OCR salvo com sucesso em: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar arquivo de texto do OCR", e);
            Toast.makeText(this, "Falha ao salvar o texto escaneado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showActionDialog() {
        final CharSequence[] options = {"Sugerir Melhorias", "Compartilhar", "Visualizar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha uma Ação");
        builder.setItems(options, (dialog, item) -> {
            String selectedOption = options[item].toString();
            switch (selectedOption) {
                case "Sugerir Melhorias":
                    if (ocrTextResult.isEmpty()) {
                        Toast.makeText(this, "Execute o 'DocScan (OCR)' primeiro para obter um texto.", Toast.LENGTH_LONG).show();
                    } else {
                        // LÓGICA REINTRODUZIDA AQUI
                        textViewActionResult.setText("Buscando sugestões...");
                        performLanguageCheck(ocrTextResult);
                    }
                    break;
                case "Compartilhar":
                    shareDocument();
                    break;
                case "Visualizar":
                    viewDocument();
                    break;
            }
        });
        builder.show();
    }

    // ===============================================================
    // LÓGICA DA LANGUAGETOOL
    // ===============================================================

    private void performLanguageCheck(final String text) {
        if (languageToolService == null) {
            languageToolService = RetrofitClient.getApiService();
        }
        Call<LanguageToolResponse> call = languageToolService.checkText("pt-BR", text);
        call.enqueue(new Callback<LanguageToolResponse>() {
            @Override
            public void onResponse(@NonNull Call<LanguageToolResponse> call, @NonNull Response<LanguageToolResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processLanguageApiResponse(response.body(), text);
                } else {
                    textViewActionResult.setText("Erro na API LanguageTool: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<LanguageToolResponse> call, @NonNull Throwable t) {
                textViewActionResult.setText("Falha na conexão com LanguageTool: " + t.getMessage());
            }
        });
    }

    private void processLanguageApiResponse(LanguageToolResponse apiResponse, String originalText) {
        if (apiResponse.getMatches() == null || apiResponse.getMatches().isEmpty()) {
            textViewActionResult.setText("Nenhum erro encontrado.\n\nTexto original:\n" + originalText);
            return;
        }
        StringBuilder suggestionsBuilder = new StringBuilder();
        suggestionsBuilder.append("Sugestões Encontradas:\n\n");
        for (LanguageToolResponse.Match match : apiResponse.getMatches()) {
            String originalWordSegment = originalText.substring(match.getOffset(), match.getOffset() + match.getLength());
            suggestionsBuilder.append("Erro: '").append(originalWordSegment).append("'\n");
            if(match.getReplacements() != null && !match.getReplacements().isEmpty()){
                suggestionsBuilder.append("  Sugestão: ").append(match.getReplacements().get(0).getValue()).append("\n\n");
            } else {
                suggestionsBuilder.append("  (Nenhuma sugestão automática)\n\n");
            }
        }
        textViewActionResult.setText(suggestionsBuilder.toString());
    }


    // ===============================================================
    // MÉTODOS DE AÇÃO COMPLETOS
    // ===============================================================

    private void shareDocument() {
        if (TextUtils.isEmpty(documentImagePath)) {
            Toast.makeText(this, "Nenhum arquivo para compartilhar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri pathUri = Uri.parse(documentImagePath);
        File fileToShare = new File(pathUri.getPath());

        if (!fileToShare.exists()) {
            // Lida com URIs de conteúdo (content://) que não são arquivos diretos
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(getMimeType(documentImagePath));
            shareIntent.putExtra(Intent.EXTRA_STREAM, pathUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via..."));
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

    private void viewDocument() {
        if (TextUtils.isEmpty(documentImagePath)) {
            Toast.makeText(this, "Nenhum arquivo para visualizar.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = Uri.parse(documentImagePath);
        String mimeType = getMimeType(documentImagePath);

        viewIntent.setDataAndType(fileUri, mimeType);
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

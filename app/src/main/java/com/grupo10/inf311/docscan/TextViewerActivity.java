package com.grupo10.inf311.docscan;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TextViewerActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);

        TextView textViewContent = findViewById(R.id.textViewContent);
        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);

        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, "Caminho do arquivo não fornecido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Converte a String do caminho para um objeto File
        Uri pathUri = Uri.parse(filePath);
        File fileToRead = new File(pathUri.getPath());

        // Lê o conteúdo do arquivo e o exibe no TextView
        String fileContent = readTextFromFile(fileToRead);
        textViewContent.setText(fileContent);
    }

    private String readTextFromFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!file.exists()) {
            return "Erro: Arquivo não encontrado.";
        }
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro ao ler o arquivo: " + e.getMessage();
        }
        return stringBuilder.toString();
    }
}
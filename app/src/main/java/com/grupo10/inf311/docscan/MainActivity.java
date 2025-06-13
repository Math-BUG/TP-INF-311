package com.grupo10.inf311.docscan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // so para teste
        //clicar no botao create account leva pra tela de cadastro
        Button btnCreateAccount = findViewById(R.id.createAccountButton);
        btnCreateAccount.setOnClickListener(v -> {
            Intent it = new Intent(this, tela_cadastro.class);
            startActivity(it);
        });
        Button btnTesteLangaugeTool = findViewById(R.id.signInButton);
        btnTesteLangaugeTool.setOnClickListener(v -> {
//            Intent it = new Intent(this, LanguageToolActivity.class);
//            startActivity(it);
            Intent it = new Intent(this, HomeActivity.class);
            startActivity(it);
        });
        Button btnOcrTeste = findViewById(R.id.forgotPassword);
        btnOcrTeste.setOnClickListener(v -> {
            Intent it = new Intent(this, OcrActivity.class);
            startActivity(it);
        });
    }
}
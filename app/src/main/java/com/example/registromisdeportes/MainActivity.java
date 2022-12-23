package com.example.registromisdeportes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    ImageButton Imagen;
    Button InicioSesion;
    EditText Password,Email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InicioSesion=findViewById(R.id.button);
        Imagen=findViewById(R.id.imageButton);
        Password=findViewById(R.id.editTextTextPassword);
        Email=findViewById(R.id.editTextTextEmailAddress);

    }
}
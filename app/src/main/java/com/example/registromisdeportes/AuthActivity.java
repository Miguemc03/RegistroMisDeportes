package com.example.registromisdeportes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class AuthActivity extends AppCompatActivity {

    ImageButton Imagen;
    Button InicioSesion,Registrarse;
    EditText Password,Email;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("message","Integracion de Firebase completa");
        mFirebaseAnalytics.logEvent("InitScreen",bundle);
        setContentView(R.layout.activity_auth);
        InicioSesion=findViewById(R.id.button);
        Imagen=findViewById(R.id.imageButton);
        Password=findViewById(R.id.editTextTextPassword);
        Email=findViewById(R.id.editTextTextEmailAddress);


        setup();

    }

    private void setup() {
        setTitle("Autenticacion");
        Registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Email.getText().toString().isEmpty() && !Password.getText().toString().isEmpty()){
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(Email.getText().toString(),Password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if ( task.isSuccessful()){
                                showHome(task.getResult().getUser().getEmail());
                            }
                            else{
                                showAlert();
                            }
                        }
                    });

                }
            }
        });
        InicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Email.getText().toString().isEmpty() && !Password.getText().toString().isEmpty()){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(Email.getText().toString(),Password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if ( task.isSuccessful()){
                                showHome(task.getResult().getUser().getEmail());
                            }
                            else{
                                showAlert();
                            }
                        }
                    });

                }
            }
        });
    }
    private  void showAlert(){
        AlertDialog.Builder builder =new  AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Se ha producido un error autenticando al usuario");
        builder.setPositiveButton("Aceptar",null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }
    private void showHome(String email){
        Intent intent =new Intent(this,HomeActivity.class);
        intent.putExtra("email",email);
        startActivity(intent);
    }
}
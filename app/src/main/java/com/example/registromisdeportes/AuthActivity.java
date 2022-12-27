package com.example.registromisdeportes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuthActivity extends AppCompatActivity {

    ImageButton Imagen;
    Button InicioSesion,Registrarse;
    EditText Password,Email;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final int VENGO_DE_LA_CAMARA = 1;
    private static final int PIDO_PERMISO_ESCRITURA = 1;
    private static final int VENGO_DE_LA_CAMARA_CON_CALIDAD = 2;
    private static final int VENGO_DE_LA_GALERIA = 3;
    File fichero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("message","Integracion de Firebase completa");
        mFirebaseAnalytics.logEvent("InitScreen",bundle);
        setContentView(R.layout.activity_auth);
        InicioSesion=findViewById(R.id.button);
        Registrarse=findViewById(R.id.button2);
        Imagen=findViewById(R.id.imageButton);
        Password=findViewById(R.id.editTextTextPassword);
        Email=findViewById(R.id.editTextTextEmailAddress);
        Imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AuthActivity.this);

                alertDialog.setTitle("Selecciona");

                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Galeria", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        startActivityForResult(galeria, VENGO_DE_LA_GALERIA);
                    }
                });
                alertDialog.setNegativeButton("Hacer Foto", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        pedirPermisoParaFoto();
                    }
                });
                alertDialog.show();
            }
        });

        setup();

    }

    private void pedirPermisoParaFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {//No tengo permiso
            //Pido permiso
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PIDO_PERMISO_ESCRITURA);
            }
        } else {
            hacerFotoCalidad();
        }
    }


    private void hacerFotoCalidad() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            fichero = crearFicheroFoto();
        } catch (IOException e) {
            e.printStackTrace();
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
                "com.example.RegistroMisDeportes.fileprovider", fichero));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, VENGO_DE_LA_CAMARA_CON_CALIDAD);
        } else {
            Toast.makeText(this, "Necitas cámara para poder hacer fotos!!", Toast.LENGTH_SHORT).show();
        }
    }
    private File crearFicheroFoto() throws IOException {
        String fechaYHora = new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date());
        String nombreFichero = "misFotos_" + fechaYHora;
        File carpetaFotos = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        carpetaFotos.mkdirs();
        File imagenAGranResolucion = File.createTempFile(nombreFichero, ".jpg", carpetaFotos);
        return imagenAGranResolucion;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VENGO_DE_LA_CAMARA && resultCode == RESULT_OK) {
            Uri uri = (Uri) data.getData();
            Imagen.setImageURI(uri);

        } else if (requestCode == VENGO_DE_LA_CAMARA_CON_CALIDAD) {
            if (resultCode == RESULT_OK) {
                Imagen.setImageURI(Uri.fromFile(fichero));

                actualizarGaleria(fichero.getAbsolutePath());
            } else {
                fichero.delete();
            }
        }else if (requestCode== VENGO_DE_LA_GALERIA){
            Uri imagenUri = data.getData();

            Imagen.setImageURI(imagenUri);
        }
    }

    void actualizarGaleria(String path){
        MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                Log.d("ACTUALIZAR", "Se ha actualizado la galería");
            }
        });
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
                                showHome();
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
                                showHome();
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
    private void showHome(){
        Intent intent =new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
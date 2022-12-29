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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

public class AuthActivity extends AppCompatActivity implements SensorEventListener {

    private static final String IMAGEN = "Imagen";
    ImageButton Imagen;
    Button InicioSesion,Registrarse;
    EditText Password,Email;
    TextView texto;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final int VENGO_DE_LA_CAMARA = 1;
    private static final int PIDO_PERMISO_ESCRITURA = 1;
    private static final int VENGO_DE_LA_CAMARA_CON_CALIDAD = 2;
    private static final int VENGO_DE_LA_GALERIA = 3;
    File fichero;
    int CONTADOR=0;
    SharedPreferences myPreferences;
    private boolean estadoImagen=false;
    SensorManager sensorManager;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(AuthActivity.this);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        setContentView(R.layout.activity_auth);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("message","Integracion de Firebase completa");
        mFirebaseAnalytics.logEvent("InitScreen",bundle);
        setContentView(R.layout.activity_auth);
        texto=findViewById(R.id.textView);
        InicioSesion=findViewById(R.id.button);
        Registrarse=findViewById(R.id.button2);
        Imagen=findViewById(R.id.imageButton);
        Password=findViewById(R.id.editTextTextPassword);
        Email=findViewById(R.id.editTextTextEmailAddress);
        mediaPlayer = MediaPlayer.create(this, R.raw.error);
        if (myPreferences.getString(IMAGEN,"").isEmpty()==false){
            Imagen.setImageURI(Uri.parse(myPreferences.getString(IMAGEN,"")));
            estadoImagen=true;
        }
        sensorManager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_NORMAL);

        Imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AuthActivity.this);

                alertDialog.setTitle("Selecciona una opcion");


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
        if (requestCode == VENGO_DE_LA_CAMARA_CON_CALIDAD) {
            if (resultCode == RESULT_OK) {
                Imagen.setImageURI(Uri.parse(fichero.getAbsolutePath()));
                myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit=myPreferences.edit();
                edit.putString(IMAGEN,fichero.getAbsolutePath());
                edit.commit();
                estadoImagen=true;
                actualizarGaleria(fichero.getAbsolutePath());
            } else {
                fichero.delete();
            }
        }else if (requestCode== VENGO_DE_LA_GALERIA){
            try {
                Uri imagenUri = data.getData();
                myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit=myPreferences.edit();
                edit.putString(IMAGEN,data.getData().toString());
                edit.commit();
                estadoImagen=true;
                Imagen.setImageURI(imagenUri);
            }
            catch (Exception e){
                e.printStackTrace();
            }

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
                if(!Email.getText().toString().isEmpty() && !Password.getText().toString().isEmpty() && estadoImagen==true){
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
        builder.setPositiveButton("Aceptar",null);
        AlertDialog dialog=builder.create();
        dialog.show();
        CONTADOR++;

        Log.v("contador",""+CONTADOR);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.vibraranimation);
        InicioSesion.startAnimation(animation);
        Vibrator vibrator=(Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);


    }
    private void showHome(){
        Intent intent =new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType()== Sensor.TYPE_GYROSCOPE){

            Sonido sonido = new Sonido();
            sonido.execute(sensorEvent);



        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    private class Sonido extends AsyncTask<SensorEvent, Void, Void> {
        String pos;

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            texto.setText(pos);
        }

        @Override
        protected Void doInBackground(SensorEvent... sensorEvents) {
            SensorEvent sensorEvent = sensorEvents[0];
            boolean sonido = true;
            pos=""+sensorEvent.values[0];
            if (CONTADOR >= 3 && sensorEvent.values[0] <= 0) {

                while (sonido) {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                    if (mediaPlayer.isPlaying() && sensorEvent.values[0] > 0) {
                        mediaPlayer.stop();
                        sonido = false;
                        CONTADOR = 0;
                    }


                }
            }
            return null;
        }
    }
}
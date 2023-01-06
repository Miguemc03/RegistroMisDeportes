package com.example.registromisdeportes.ui.settings;

import static android.content.Context.NOTIFICATION_SERVICE;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.registromisdeportes.Global;
import com.example.registromisdeportes.ManejadorBD;
import com.example.registromisdeportes.R;
import com.example.registromisdeportes.databinding.FragmentSettingsBinding;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends Fragment implements SensorEventListener {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




    private static final String ID_CANAL = "Nombre del canal";
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    ManejadorBD manejadorBD;
    Button silenciar,compartir;
    TextView textViewLogros;
    String texto="";
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manejadorBD = new ManejadorBD(getContext());
        silenciar=view.findViewById(R.id.buttonSilenciar);
        compartir=view.findViewById(R.id.buttonCompartir);
        textViewLogros=view.findViewById(R.id.textViewLogros);
        Logros();
        senSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        textViewLogros.setText(texto);
        if (Global.sonido==true){
            silenciar.setText("silenciar");
        }
        else {
            silenciar.setText("desenmudecer");
        }
        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                intent.putExtra(Intent.EXTRA_TEXT, texto);

                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    ex.printStackTrace();

                    Snackbar.make(view, "El dispositivo no tiene instalado WhatsApp", Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
        silenciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Global.sonido==true){
                    Global.sonido=false;
                    silenciar.setText("desenmudecer");
                }
                else {
                    Global.sonido=true;
                    silenciar.setText("silenciar");
                }

            }
        });

    }

    private void Logros() {

        Cursor cursor=manejadorBD.getDeportes();
        if(cursor!=null){

            while (cursor.moveToNext()){
                texto+=cursor.getString(0)+": ";
                int id=cursor.getInt(1);
                Cursor cursor2=manejadorBD.getDuracion(id);
                cursor2.moveToFirst();
                int tiempo=cursor2.getInt(0);
                Log.v("Tiempo",""+tiempo);
                int minutos =  tiempo/60;
                int segundos = tiempo %60;
                String salida = String.format("%02d:%02d", minutos, segundos);
                texto+=salida+"\n";
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    try {
                        MostrarNotificacion();
                    }
                    catch (Exception e){

                    }

                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    private void MostrarNotificacion() {
        String idChannnel = "Canal 4";
        String nombreCanal = "Canal con Foto";
        String idAct="",dep="";
        int id =id();
        Cursor cursor=manejadorBD.getDeporte(id);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            dep=cursor.getString(0);
        }
        cursor.close();



        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), ID_CANAL);


        builder.setSmallIcon(R.drawable.corriendo).
                setContentTitle("Es buen momento para hacer: "+dep).
                setAutoCancel(false);

        /*NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        Bitmap bitmapAlbert = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        bigPictureStyle.bigPicture(bitmapAlbert);
        bigPictureStyle.setBigContentTitle("Albert, un tío listo");
        bigPictureStyle.setSummaryText("Sabía mucho este hombre");

        builder.setStyle(bigPictureStyle);*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(idChannnel, nombreCanal, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setShowBadge(true);
            builder.setChannelId(idChannnel);
            notificationManager.createNotificationChannel(notificationChannel);

        } else {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        }
        notificationManager.notify(1, builder.build());
    }

    private int id() {
        Cursor cursor=manejadorBD.getNDeportes();
        cursor.moveToFirst();
        int max=cursor.getInt(0);
        int min=1;

        int x = (int) (Math.random()*((max-min)+1)+min);
        Log.v("n",x+"");
        return x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
package com.example.registromisdeportes.ui.dashboard;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.registromisdeportes.Global;
import com.example.registromisdeportes.ManejadorBD;
import com.example.registromisdeportes.R;

import com.example.registromisdeportes.databinding.FragmentDashboardBinding;
import com.example.registromisdeportes.ui.home.HomeFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




    ManejadorBD manejadorBD;
    EditText editTextMinutos,editTextSegundos;
    TextView crono;
    Spinner spinnerDeportes;
    Button buttonIniciar;
    ArrayAdapter<String> arrayAdapter;
    List<String> lista = new ArrayList<>();
    private static final long TIEMPO_REFRESCO = 1000;
    private static final int PERMISO_GPS = 123;
    LocationManager locationManager;
    LocationListener locationListener;
    Integer id=1;
    final Double[] Longitud = new Double[1];
    final Double[] Latitud = new Double[1];
    MediaPlayer mediaPlayer;
    private static final String ID_CANAL = "Nombre del canal";
    private static final int CODIGO_RESPUESTA = 123;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manejadorBD = new ManejadorBD(getContext());
        editTextMinutos=view.findViewById(R.id.editTextMinutos);
        editTextSegundos=view.findViewById(R.id.editTextSegundos);
        spinnerDeportes=view.findViewById(R.id.spinnerDeportes);
        buttonIniciar=view.findViewById(R.id.buttonIniciar);
        crono=view.findViewById(R.id.textViewCrono);
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.correcto);
        Listar();
        locationManager =(LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Longitud[0] =location.getLongitude();
                Latitud[0] =location.getLatitude();
            }
        };
        if (Longitud[0]!=null){
            Toast.makeText(getContext(),"Ubicacion obtenida",Toast.LENGTH_SHORT).show();
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISO_GPS);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIEMPO_REFRESCO, 0, locationListener);
        buttonIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CrearActividad();
            }
        });
    }

    private void CrearActividad() {
        String fecha=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String hora=new SimpleDateFormat("HH:mm:ss").format(new Date());
        Integer duracion=0;
        try {
            duracion=Integer.parseInt(editTextMinutos.getText().toString())*60+Integer.parseInt(editTextSegundos.getText().toString());
        }catch (Exception e){
            Toast.makeText(getContext(),"Los datos de tiempo estan vacios",Toast.LENGTH_SHORT).show();
        }
        if (Longitud[0]!=null){
            boolean resultado = manejadorBD.crearActividad(id,fecha,hora,Latitud[0],Longitud[0],duracion);

            if (resultado) {
                Toast.makeText(getContext(), "Se ha insertado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error en la inserción", Toast.LENGTH_SHORT).show();
            }
            mediaPlayer.start();
            MiCronometro mc = new MiCronometro(duracion, crono);
            mc.execute();
        }
        else {
            Toast.makeText(getContext(),"Obteniendo ubicacion espera un momento",Toast.LENGTH_SHORT).show();
        }


    }
    
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISO_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIEMPO_REFRESCO, 0, locationListener);
            } else {
                Toast.makeText(getContext(), "Esta aplicación necesita este permiso para funcionar.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void Listar() {
        lista.clear();
        Cursor cursor = manejadorBD.listar();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String fila = cursor.getString(1);

                lista.add(fila);
            }
            arrayAdapter = new ArrayAdapter<>(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, lista);
            spinnerDeportes.setAdapter(arrayAdapter);

        } else{
            arrayAdapter = new ArrayAdapter<>(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, lista);
            spinnerDeportes.setAdapter(arrayAdapter);
        }
        spinnerDeportes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    cursor.moveToPrevious();
                    while (cursor.moveToNext()) {
                        if (cursor.getString(1).compareTo(adapterView.getSelectedItem().toString())==0){
                            id=Integer.parseInt(cursor.getString(0));
                        }
                    }
                    if (Global.sonido){

                    }
                }
                //Toast.makeText(getContext(), "Has seleccionado el deporte: " + adapterView.getSelectedItem().toString()+" y su id es:"+id, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private class MiCronometro extends AsyncTask<String, String, String> {
        int miContador;
        TextView miTextView;
        MiCronometro(int inicio, TextView tv){
            miContador = inicio;
            miTextView = tv;

        }
        @Override
        protected void onPostExecute(String s) {

            lazarNotificacionConFoto();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            miTextView.setText(values[0]);
        }

        @Override
        protected String doInBackground(String... strings) {
            while(miContador!=-1){
                int minutos = miContador /60;
                int segundos = miContador %60;
                String salida = String.format("%02d:%02d", minutos, segundos);
                publishProgress(salida);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                miContador--;
            }
            return null;

        }
    }
    private void lazarNotificacionConFoto() {
        String idChannnel = "Canal 4";
        String nombreCanal = "Canal con Foto";
        String idAct="",dep="";
        int tiempo=0;
        Cursor cursor = manejadorBD.UltimaActividad();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                idAct = cursor.getString(0);

            }
        }
        cursor.close();
         cursor=manejadorBD.getDeporte(id);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            dep=cursor.getString(0);
        }
        cursor.close();
        String fecha=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Log.v("fecha",fecha);
        cursor=manejadorBD.getTiempo();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            tiempo=cursor.getInt(0);
            Log.v("Duracion",""+tiempo);
        }

        int min=tiempo/60;
        int seg=tiempo%60;

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), ID_CANAL);


        builder.setSmallIcon(R.mipmap.ic_launcher).
                setContentTitle("Has finalizado tu actividad de: "+dep).
                setAutoCancel(false).
                setContentText("Hoy llevas un total de: "+min+":"+seg);

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
}
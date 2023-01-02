package com.example.registromisdeportes.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.registromisdeportes.ManejadorBD;
import com.example.registromisdeportes.R;
import com.example.registromisdeportes.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    ManejadorBD manejadorBD;
    ListView listView;
    List<Actividad> lista = new ArrayList<>();
    List<String> listaString = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    ArrayList<Actividad> actividades;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView=view.findViewById(R.id.listViewActividades);
        manejadorBD = new ManejadorBD(getContext());
        actividades = new ArrayList<>();
        Listar();
    }

    private void Listar() {
        lista.clear();
        Cursor cursor = manejadorBD.listarActividad();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                Actividad actividad = new Actividad(cursor.getInt(0),cursor.getInt(1),cursor.getString(2),cursor.getString(3),cursor.getDouble(4),cursor.getDouble(5),cursor.getInt(6));
                listaString.add(cursor.getString(2));
                lista.add(actividad);
                actividades.add(actividad);
            }
            AdaptadorParaActividades adaptadorParaActividades = new AdaptadorParaActividades(getContext(), R.layout.actividades, listaString);
            listView.setAdapter(adaptadorParaActividades);

        } else{
            AdaptadorParaActividades adaptadorParaActividades = new AdaptadorParaActividades(getContext(), R.layout.actividades, listaString);
            listView.setAdapter(adaptadorParaActividades);
        }
    }

    public class AdaptadorParaActividades extends ArrayAdapter<String>{

        public AdaptadorParaActividades(@NonNull Context context, int resource, List<String> lista) {
            super(context, resource, lista);
        }
        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return rellenarFila(position, convertView, parent);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return rellenarFila(position, convertView, parent);
        }

        private View rellenarFila(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View miFila = inflater.inflate(R.layout.actividades,parent, false);

            TextView deporte = miFila.findViewById(R.id.textViewDeporte);
            TextView fecha = miFila.findViewById(R.id.textViewFecha);
            TextView hora = miFila.findViewById(R.id.textViewHora);
            TextView duracion = miFila.findViewById(R.id.textViewDuracion);
            Button localizar=miFila.findViewById(R.id.buttonLocalizar);
            Actividad actividad=lista.get(position);
            Cursor cursor=manejadorBD.getDeporte(actividad.getIdDe());
            cursor.moveToFirst();

            deporte.setText(cursor.getString(0));

            fecha.setText(actividad.getFecha());
            hora.setText(actividad.getHora());
            int miContador=actividad.getDuracion();
            int minutos = miContador /60;
            int segundos = miContador %60;
            String salida = String.format("%02d:%02d", minutos, segundos);
            duracion.setText(salida);
            localizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:"+actividad.getLatitud()+","+actividad.getLongitud()+"?z=16&q="+actividad.getLatitud()+","+actividad.getLongitud()+"("+cursor.getString(0)+")"));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                }
            });
            return miFila;
        }
    }
    class Actividad {
        int id,idDe,duracion;
        Double latitud,longitud;
        String fecha,hora;

        public Actividad(int id, int idDe, String fecha, String hora, double latitud, double longitud, int duracion) {
            this.id = id;
            this.idDe = idDe;
            this.fecha = fecha;
            this.hora = hora;
            this.latitud = latitud;
            this.longitud = longitud;
            this.duracion = duracion;
        }

        public int getId() {
            return id;
        }

        public int getIdDe() {
            return idDe;
        }

        public String getFecha() {
            return fecha;
        }

        public String getHora() {
            return hora;
        }

        public Double getLatitud() {
            return latitud;
        }

        public Double getLongitud() {
            return longitud;
        }

        public int getDuracion() {
            return duracion;
        }
    }
}
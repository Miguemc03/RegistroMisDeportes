package com.example.registromisdeportes.ui.home;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.registromisdeportes.ManejadorBD;
import com.example.registromisdeportes.R;
import com.example.registromisdeportes.databinding.FragmentHomeBinding;


import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    EditText editTextId, editTextDeporte, editTextDescripcion;
    Button buttonGuardar, buttonBorrar, buttonActualizar;
    ListView listView;
    ArrayList<Deporte> deportes;
    ManejadorBD manejadorBD;

    ArrayAdapter<String> arrayAdapter;
    List<String> lista = new ArrayList<>();
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manejadorBD = new ManejadorBD(getContext());
        editTextId = view.findViewById(R.id.editTextID);
        editTextDeporte = view.findViewById(R.id.editTextDeporte);
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion);
        buttonActualizar = view.findViewById(R.id.buttonActualizar);
        buttonBorrar = view.findViewById(R.id.buttonBorrar);
        buttonGuardar = view.findViewById(R.id.buttonGuardar);
        listView = view.findViewById(R.id.listView);
        deportes = new ArrayList<>();


        Listar();


        buttonActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean resultado = manejadorBD.actualizar(editTextId.getText().toString(), editTextDeporte.getText().toString(), editTextDescripcion.getText().toString());
                Toast.makeText(getContext(), resultado?"Modificado correctamente":"No se ha modificado nada", Toast.LENGTH_SHORT).show();
                Listar();
            }
        });
        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean resultado = manejadorBD.insertar(editTextDeporte.getText().toString(), editTextDescripcion.getText().toString());

                if (resultado) {
                    Toast.makeText(getContext(), "Se ha insertado correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error en la inserción", Toast.LENGTH_SHORT).show();
                }
                Listar();

            }
        });
        buttonBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Usando un alertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


                builder.setMessage("Deseas Borrar este DEPORTE "+editTextDeporte.getText().toString()+"?");
                builder.setCancelable(false);
                builder.setNegativeButton("No borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(), "No se ha borrado", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean borrado = manejadorBD.borrar(editTextId.getText().toString());
                        Toast.makeText(getContext(), borrado?"Borrado correctamente":"Nada a borrar", Toast.LENGTH_SHORT).show();
                        Listar();
                    }
                });

                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("¡ ATENCIÓN !");
                alert.show();

            }
        });
    }

    private void Listar() {
        lista.clear();
        deportes.clear();
        Cursor cursor = manejadorBD.listar();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Deporte deporte = new Deporte(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                String fila = deporte.toString();
                lista.add(fila);
                deportes.add(deporte);
            }
            arrayAdapter = new ArrayAdapter<>(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, lista);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    editTextId.setText(deportes.get(i).getId());
                    editTextDeporte.setText(deportes.get(i).getDeporte());
                    editTextDescripcion.setText(deportes.get(i).getExplicacion());

                }
            });
        } else{
            arrayAdapter = new ArrayAdapter<>(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, lista);
            listView.setAdapter(arrayAdapter);

        }
    }

    class Deporte {
        String id, deporte, explicacion;

        @Override
        public String toString() {
            return "id=" + id +", deporte=" + deporte + ", explicacion=" + explicacion ;
        }

        public Deporte(String id, String deporte, String explicacion) {
            this.id = id;
            this.deporte = deporte;
            this.explicacion = explicacion;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDeporte() {
            return deporte;
        }

        public void setDeporte(String deporte) {
            this.deporte = deporte;
        }

        public String getExplicacion() {
            return explicacion;
        }

        public void setExplicacion(String explicacion) {
            this.explicacion = explicacion;
        }
    }
}
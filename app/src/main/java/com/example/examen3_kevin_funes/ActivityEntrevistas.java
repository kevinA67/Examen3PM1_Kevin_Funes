package com.example.examen3_kevin_funes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;

import com.example.examen3_kevin_funes.Config.Entrevistas;
import com.example.examen3_kevin_funes.Config.ListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActivityEntrevistas extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    List<Entrevistas> listEntrevistas;
    ListAdapter listAdapter;
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrevistas);
        firebaseFirestore = FirebaseFirestore.getInstance();

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        ObtenerDatos();
    }

    private void ObtenerDatos() {
        firebaseFirestore.collection("entrevistas")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        listEntrevistas = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            try {
                                Entrevistas entrevista = new Entrevistas();
                                //Personas persona = documentSnapshot.toObject(Personas.class);
                                // Obtener el ID del documento
                                entrevista.setId(documentSnapshot.getId());
                                entrevista.setPeriodista(documentSnapshot.get("periodista").toString());
                                entrevista.setDescripcion(documentSnapshot.get("descripcion").toString());
                                entrevista.setFecha(documentSnapshot.get("fecha").toString());
                                entrevista.setImagen(documentSnapshot.get("imagen").toString());

                                listEntrevistas.add(entrevista);
                                //listPersonas.add(new Personas(persona.getId(), persona.getNombres(), persona.getApellidos(), persona.getCorreo(), persona.getFechanac()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        llenarLista();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Manejar errores
                    }
                });
    }

    private void llenarLista() {
//        List<Personas> personas=new ArrayList<>();
//        personas.add(new Personas("Kevin"));
//        personas.add(new Personas("Alexis"));

        listAdapter = new ListAdapter(listEntrevistas, this);
        RecyclerView recyclerView = findViewById(R.id.listRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);
    }

    private void filter(String text) {
        List<Entrevistas> filteredList = new ArrayList<>();
        for (Entrevistas entrevistas : listEntrevistas) {
            String nombre = entrevistas.getPeriodista() + " " + entrevistas.getDescripcion()+ " " +entrevistas.getFecha();
            if (nombre.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(entrevistas);
            }
        }

        if (!filteredList.isEmpty()) {
            listAdapter.setFilteredList(filteredList);
        }
    }
}
package com.example.examen3_kevin_funes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.examen3_kevin_funes.Config.Entrevistas;
import com.example.examen3_kevin_funes.Config.ListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class ActivityEntrevistas extends AppCompatActivity {
    private StorageReference storageRef;
    FirebaseFirestore firebaseFirestore;
    List<Entrevistas> listEntrevistas;
    ListAdapter listAdapter;
    SearchView searchView;
    Button btnEliminar, btnVerEntrevista;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrevistas);
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.clearFocus();
        btnEliminar=(Button) findViewById(R.id.btnEliminar);
        btnVerEntrevista=(Button) findViewById(R.id.btnVer);


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

        btnEliminar.setOnClickListener(View ->{
            if (ListAdapter.getSelectedItem() != -1) {
                // Mostrar un diálogo de confirmación de eliminación
                alertaEliminar();
            } else {
                // Mostrar un mensaje si no se ha seleccionado ningún contacto
                Toast.makeText(ActivityEntrevistas.this, "Selecciona una entrevista primero", Toast.LENGTH_SHORT).show();
            }
        });

        btnVerEntrevista.setOnClickListener(View ->{
            if (ListAdapter.getSelectedItem() != -1) {
                Intent intent=new Intent(getApplicationContext(),ActivityVerEntrevista.class);
                // Obtener el contacto seleccionado
                int selectedItemIndex = ListAdapter.getSelectedItem();
                if (selectedItemIndex != -1) {
                    Entrevistas entrevistas = listEntrevistas.get(selectedItemIndex);
                    intent.putExtra("id", entrevistas.getId());
                    intent.putExtra("periodista", entrevistas.getPeriodista());
                    intent.putExtra("descripcion", entrevistas.getDescripcion());
                    intent.putExtra("fecha", entrevistas.getFecha());
                    intent.putExtra("imagen", entrevistas.getImagen());
                    intent.putExtra("audio", entrevistas.getAudio());
                }
                startActivity(intent);
            } else {
                // Mostrar un mensaje si no se ha seleccionado ningún contacto
                Toast.makeText(ActivityEntrevistas.this, "Selecciona una entrevista primero", Toast.LENGTH_SHORT).show();
            }

        });
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
                                entrevista.setAudio(documentSnapshot.get("audio").toString());

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

    private void alertaEliminar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEntrevistas.this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Desea eliminar los datos de la entrevista seleccionada?");

        // Agregar botón de actualizar
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtener el contacto seleccionado
                int selectedItemIndex = ListAdapter.getSelectedItem();
                if (selectedItemIndex != -1) {
                    Entrevistas entrevistas = listEntrevistas.get(selectedItemIndex);
                    eliminarEntrevista(entrevistas);
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Si el usuario cancela la eliminación, no hacer nada
            }
        });

        builder.show();
    }

    private void eliminarEntrevista(Entrevistas entrevistas) {
        // Eliminar la persona de la colección "personas" en Firestore
        firebaseFirestore.collection("entrevistas")
                .document(entrevistas.getId()) // Utilizamos el ID de la persona para identificar el documento a eliminar
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // La persona ha sido eliminada exitosamente
                        Toast.makeText(getApplicationContext(), "Entrevista eliminada exitosamente", Toast.LENGTH_SHORT).show();

                        // Obtener el nombre del archivo de audio y de imagen de la entrevista eliminada
                        String audioFileName = getNombreArchivo(entrevistas.getAudio());
                        String imageFileName =  getNombreArchivo(entrevistas.getImagen());

                        // Eliminar el archivo de audio del almacenamiento
                        if (audioFileName != null) {
                            StorageReference audioRef = storageRef.child("entrevistas").child(audioFileName);
                            audioRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // El archivo de audio ha sido eliminado exitosamente
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error al eliminar el archivo de audio
                                }
                            });
                        }

                        // Eliminar el archivo de imagen del almacenamiento
                        if (imageFileName != null) {
                            StorageReference imageRef = storageRef.child("entrevistas").child(imageFileName);
                            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // El archivo de imagen ha sido eliminado exitosamente
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error al eliminar el archivo de imagen
                                }
                            });
                        }

                        // Volver a cargar los datos después de eliminar la entrevista
                        ObtenerDatos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Ocurrió un error al intentar eliminar la persona
                        Toast.makeText(getApplicationContext(), "Error al eliminar entrevista: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Puedes manejar el error de acuerdo a tus necesidades
                    }
                });
    }

    // Método para extraer el nombre del archivo de la URL de descarga
    private static String getNombreArchivo(String storageUrl) {
        // Dividir la URL por el carácter "/"
        String[] parts = storageUrl.split("/");
        // Obtener el último elemento que debería ser el nombre del archivo
        String fileName = parts[parts.length - 1];
        // Si el nombre del archivo contiene "?", dividirlo nuevamente para eliminar los parámetros adicionales de la URL
        if (fileName.contains("?")) {
            fileName = fileName.split("\\?")[0];
        }
        // Si el nombre del archivo comienza con "entrevistas%2F", eliminar este prefijo
        if (fileName.startsWith("entrevistas%2F")) {
            fileName = fileName.substring("entrevistas%2F".length());
        }
        // Decodificar la URL para reemplazar %20 con espacios en blanco
        try {
            fileName = URLDecoder.decode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fileName;
    }



}
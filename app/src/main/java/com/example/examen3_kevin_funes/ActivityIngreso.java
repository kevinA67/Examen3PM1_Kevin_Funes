package com.example.examen3_kevin_funes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.examen3_kevin_funes.Config.Entrevistas;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;

public class ActivityIngreso extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    static final int ACCESS_CAMERA = 201;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageRef;
    Button btnEntrevistas, btnTomarFoto, btnRegistrar;
    EditText periodista, descripcion, fecha;
    ImageView imageView;
    Uri imageUri;
    String urlImagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);

        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        btnEntrevistas = (Button) findViewById(R.id.btnEntrevistas);
        fecha = (EditText) findViewById(R.id.txtFecha);
        btnTomarFoto = (Button) findViewById(R.id.btnTomarFoto);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);
        periodista = (EditText) findViewById(R.id.txtNombre);
        descripcion = (EditText) findViewById(R.id.txtDescripcion);

        btnEntrevistas.setOnClickListener(View -> {
            Intent intent = new Intent(getApplicationContext(), ActivityEntrevistas.class);
            startActivity(intent);
        });

        fecha.setOnClickListener(View -> {
            mostrarCalendario();
        });

        btnTomarFoto.setOnClickListener(View -> {
            permisosCamara();
        });

        btnRegistrar.setOnClickListener(View -> {
            String descripcion = this.descripcion.getText().toString().trim();
            String periodista = this.periodista.getText().toString().trim();
            String fecha = this.fecha.getText().toString().trim();

            if (imageView.getDrawable() == null || descripcion.isEmpty() || periodista.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Llenar todos los campos.", Toast.LENGTH_LONG).show();
            } else {
                subirImagen(imageUri, new UploadCallback() {
                    @Override
                    public void onUploadSuccess(String imageUrl) {
                        Entrevistas entrevistas = new Entrevistas(periodista, descripcion, fecha, imageUrl);
                        crearEntrevista(entrevistas);
                    }
                    @Override
                    public void onUploadFailure(String errorMessage) {
                        // Ocurrió un error durante la subida de la imagen
                        Toast.makeText(getApplicationContext(), "Error al subir la imagen: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    public interface UploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(String errorMessage);
    }
    private void subirImagen(Uri uri, UploadCallback callback) {
        if (uri != null) {
            File imageFile = getFileFromUri(uri);
            if (imageFile != null) {
                StorageReference imagesRef = storageRef.child("entrevistas").child(imageFile.getName());
                UploadTask uploadTask = imagesRef.putFile(Uri.fromFile(imageFile));

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Guarda la URL de descarga en Firestore
                                String imageUrl = uri.toString();
                                callback.onUploadSuccess(imageUrl);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Ocurrió un error al subir la imagen
                                Toast.makeText(getApplicationContext(), "Error al subir la imagen.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }
    }

    private void crearEntrevista(Entrevistas entrevistas) {
        // Agregar el objeto nuevaPersona a la colección "personas" en Firestore
        firebaseFirestore.collection("entrevistas")
                .add(entrevistas)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // El documento ha sido creado exitosamente
                        Toast.makeText(getApplicationContext(), "Entrevista creada exitosamente", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Ocurrió un error al intentar crear el documento
                        Toast.makeText(getApplicationContext(), "Error al crear la entrevista: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void limpiarCampos() {
        periodista.setText("");
        descripcion.setText("");
        fecha.setText("");
        periodista.setFocusableInTouchMode(true);
        periodista.requestFocus();
        if(imageView!=null){
            imageView.setImageURI(null);
        }
    }

    // Método para mostrar el selector de fecha
    private void mostrarCalendario() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.DialogTheme,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fecha.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // Método para tomar una foto con la cámara
    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void permisosCamara() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, ACCESS_CAMERA);
        } else {
            tomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACCESS_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            } else {
                Toast.makeText(getApplicationContext(), "Se necesita permiso de la camara.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para procesar el resultado de la actividad de captura de imagen o selección de imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) { // Si se tomó una foto con la cámara
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageUri = getImageUri(getApplicationContext(), imageBitmap);
                imageView.setImageURI(imageUri);
            } else if (requestCode == REQUEST_IMAGE_PICK) { // Si se seleccionó una imagen desde el almacenamiento

            }
        }
    }

    // Método para convertir un Bitmap en una URI
    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    // Método para convertir una URI en un File
    private File getFileFromUri(Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow("_data");
                    filePath = cursor.getString(columnIndex);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equals(uri.getScheme())) {
            filePath = uri.getPath();
        }
        if (filePath != null) {
            return new File(filePath);
        }
        return null;
    }
}

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
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ActivityIngreso extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    static final int ACCESS_CAMERA = 201;
    static final int ACCESS_AUDIO = 200;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageRef;
    Button btnEntrevistas, btnTomarFoto, btnRegistrar;
    EditText periodista, descripcion, fecha;
    TextView txtGrabar;
    ImageView imageView, btnGrabar, btnReproducir;
    Uri imageUri;
    MediaRecorder grabacion;
    String audioFilePath;
    MediaPlayer mediaPlayer;

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
        btnGrabar = (ImageView) findViewById(R.id.btnGrabar);
        btnReproducir = (ImageView) findViewById(R.id.btnReproducir);
        txtGrabar = (TextView) findViewById(R.id.txtGrabar);


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

        btnGrabar.setOnClickListener(View -> {
            permisosGrabarAudio();
        });

        btnReproducir.setOnClickListener(View -> {
            reproducirGrabacionAudio();
        });


        btnRegistrar.setOnClickListener(View -> {
            String descripcion = this.descripcion.getText().toString().trim();
            String periodista = this.periodista.getText().toString().trim();
            String fecha = this.fecha.getText().toString().trim();

            if (imageView.getDrawable() == null  || (audioFilePath == null || audioFilePath.isEmpty())|| descripcion.isEmpty() || periodista.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Llenar todos los campos.", Toast.LENGTH_LONG).show();
            }  else {
                subirAudio(audioFilePath, new UploadCallback() {
                    @Override
                    public void onUploadSuccess(String audioUrl) {
                        subirImagen(imageUri, new UploadCallback() {
                            @Override
                            public void onUploadSuccess(String imagenUrl) {
                                Entrevistas entrevistas = new Entrevistas(periodista, descripcion, fecha, imagenUrl, audioUrl);
                                crearEntrevista(entrevistas);
                            }

                            @Override
                            public void onUploadFailure(String errorMessage) {
                                // Ocurrió un error durante la subida de la imagen
                                Toast.makeText(getApplicationContext(), "Error al subir la imagen: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });

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
        void onUploadSuccess(String url);
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

    private void subirAudio(String audioFilePath, UploadCallback callback) {
        if (audioFilePath != null) {
            File audioFile = new File(audioFilePath);
            Uri audioUri = Uri.fromFile(audioFile);

            // Verificar que el archivo exista
            if (audioFile.exists()) {
                // Subir el archivo de audio a Firebase Storage
                StorageReference audiosRef = storageRef.child("entrevistas").child(audioFile.getName());
                UploadTask uploadTask = audiosRef.putFile(audioUri);

                // Escuchar el resultado de la subida
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Obtener la URL de descarga del archivo subido
                    audiosRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String audioUrl = uri.toString();
                        callback.onUploadSuccess(audioUrl);

                    }).addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Error al obtener la URL de descarga del audio.", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error al subir el audio.", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(getApplicationContext(), "El archivo de audio no existe.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "La ruta del archivo de audio es nula.", Toast.LENGTH_SHORT).show();
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
        if(imageView.getDrawable()!=null){
            imageView.setImageDrawable(null);
        }
        audioFilePath=null;
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



    private void permisosCamara() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, ACCESS_CAMERA);
        } else {
            tomarFoto();
        }
    }

    // Método para tomar una foto con la cámara
    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void permisosGrabarAudio() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, ACCESS_AUDIO);
        } else {
            iniciarGrabacionAudio();
        }
    }

    private void iniciarGrabacionAudio() {
        // Primero, asegúrate de que la variable mediaRecorder esté inicializada
        if (grabacion == null) {
            grabacion = new MediaRecorder();

            try {
                // Obtener la hora actual
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String horaActual = dateFormat.format(new Date());

                // Configurar la fuente de audio, el formato de salida y el archivo de salida
                audioFilePath = getExternalCacheDir().getAbsolutePath() +"/grabacion_audio_" + horaActual + ".mp3";
                grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
                grabacion.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                grabacion.setOutputFile(audioFilePath);
                grabacion.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

                // Preparar la grabadora para la grabación
                grabacion.prepare();

                // Iniciar la grabación
                grabacion.start();

                // Mostrar un mensaje de confirmación de inicio de grabación si lo deseas
                btnGrabar.setImageResource(R.drawable.grabardos);
                Toast.makeText(getApplicationContext(), "Grabación de audio iniciada", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                // Manejar cualquier excepción de IO aquí
            }

        } else {
            // Si ya está inicializado, detén cualquier grabación en curso
            grabacion.stop();
            grabacion.release();
            grabacion=null;
            btnGrabar.setImageResource(R.drawable.grabaruno);
            Toast.makeText(getApplicationContext(), "Grabación finalizada", Toast.LENGTH_SHORT).show();
        }
    }

    private void reproducirGrabacionAudio() {
        if (audioFilePath != null) {
            // Hay una grabación previamente realizada, así que reproducimos el audio
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(audioFilePath);
                    mediaPlayer.prepare();
                } else {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(audioFilePath);
                    mediaPlayer.prepare();
                }
                mediaPlayer.start();
                Toast.makeText(getApplicationContext(), "Reproduciendo audio", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                // Manejar cualquier excepción de IO aquí
            }
        } else {
            // No hay grabación previamente realizada, mostrar un mensaje de tostada
            Toast.makeText(getApplicationContext(), "No hay grabación de audio disponible", Toast.LENGTH_SHORT).show();
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

        if (requestCode == ACCESS_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarGrabacionAudio();
            } else {
                Toast.makeText(getApplicationContext(), "Se necesita permiso para grabar audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para procesar el resultado de la actividad de captura de imagen o selección de imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Obtén la imagen capturada del intent
                Bundle extras = data.getExtras();
                if (extras != null) {
                    // Asigna la imagen al ImageView
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageUri = getImageUri(getApplicationContext(), imageBitmap);
                    imageView.setImageBitmap(imageBitmap);
                }
            }
        }
    }

    // Método para convertir un Bitmap en una URI
    private Uri getImageUri(Context context, Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
            if (path != null) {
                return Uri.parse(path);
            }
        }
        return null;
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

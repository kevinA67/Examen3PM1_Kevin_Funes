package com.example.examen3_kevin_funes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;

public class ActivityVerEntrevista extends AppCompatActivity {

    EditText txtPeriodista,txtFecha, txtDescripcion;
    ImageView imagen, btnReproducir;
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_entrevista);

        txtDescripcion=(EditText) findViewById(R.id.txtDescripcionVer);
        txtFecha=(EditText) findViewById(R.id.txtFechaVer);
        txtPeriodista=(EditText) findViewById(R.id.txtNombreVer);
        imagen=(ImageView) findViewById(R.id.imageViewVer);
        btnReproducir=(ImageView) findViewById(R.id.btnReproducirVer);

        Intent intent=getIntent();
        txtPeriodista.setText(intent.getStringExtra("periodista"));
        txtDescripcion.setText(intent.getStringExtra("descripcion"));
        txtFecha.setText(intent.getStringExtra("fecha"));
        String imagen=intent.getStringExtra("imagen");
        String audio=intent.getStringExtra("audio");

        Glide.with(getApplicationContext())
                .load(imagen)
                .apply(new RequestOptions().override(356, 189)) // Opcional: ajusta el tamaño de la imagen si es necesario
                .into(this.imagen);

        btnReproducir.setOnClickListener(View ->{
            reproducirAudioDesdeUrl(audio);
        });
    }


    private void reproducirAudioDesdeUrl(String audioUrl) {
        mediaPlayer = new MediaPlayer();

        // Configura los atributos de audio para el MediaPlayer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mediaPlayer.setAudioAttributes(attributes);
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        try {
            // Establece la URL del audio que se va a reproducir
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(getApplicationContext(), "Reproduciendo audio", Toast.LENGTH_SHORT).show();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(getApplicationContext(), "Reproducción terminada", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error al reproducir el audio.", Toast.LENGTH_SHORT).show();
        }
    }
}
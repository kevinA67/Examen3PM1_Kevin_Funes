package com.example.examen3_kevin_funes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ActivityVerEntrevista extends AppCompatActivity {

    EditText txtPeriodista,txtFecha, txtDescripcion;
    ImageView imagen;
    Button btnReproducir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_entrevista);

        txtDescripcion=(EditText) findViewById(R.id.txtDescripcionVer);
        txtFecha=(EditText) findViewById(R.id.txtFechaVer);
        txtPeriodista=(EditText) findViewById(R.id.txtNombreVer);
        imagen=(ImageView) findViewById(R.id.imageViewVer);

        Intent intent=getIntent();
        txtPeriodista.setText(intent.getStringExtra("periodista"));
        txtDescripcion.setText(intent.getStringExtra("descripcion"));
        txtFecha.setText(intent.getStringExtra("fecha"));
        String imagen=intent.getStringExtra("imagen");

        Glide.with(getApplicationContext())
                .load(imagen)
                .apply(new RequestOptions().override(356, 189)) // Opcional: ajusta el tamaño de la imagen si es necesario
                .into(this.imagen);
    }
}
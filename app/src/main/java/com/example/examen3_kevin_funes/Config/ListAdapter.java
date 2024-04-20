package com.example.examen3_kevin_funes.Config;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.examen3_kevin_funes.R;

import java.io.File;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<Entrevistas> datos;
    private LayoutInflater inflater;
    private Context context;
    public static int selectedItem = -1;

    public ListAdapter(List<Entrevistas> itemList, Context context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.datos = itemList;
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    public static int getSelectedItem() {
        return selectedItem;
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewtype) {
        View view = inflater.inflate(R.layout.disenio, null);
        return new ListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, final int position) {
        holder.bindData(datos.get(position));

        //PARA QUE EL SELECTOR FUNCIONE
        final int currentPosition = position;
        holder.itemView.setSelected(position == selectedItem);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Actualizar el índice del elemento seleccionado
                selectedItem = currentPosition;

                // Notificar al adaptador de los cambios
                notifyDataSetChanged();
            }
        });
    }

    public void setItems(List<Entrevistas> items) {
        datos = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView periodista, descripcion, fecha;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView2);
            periodista = (TextView) itemView.findViewById(R.id.nombre);
            descripcion = (TextView) itemView.findViewById(R.id.txtDescripcion);
            fecha = (TextView) itemView.findViewById(R.id.txtFecha);
        }

        void bindData(final Entrevistas entrevistas) {
            periodista.setText(entrevistas.getPeriodista());
            descripcion.setText(entrevistas.getDescripcion());
            fecha.setText(entrevistas.getFecha());
//            File foto=new File(entrevistas.getImagen());
//            imageView.setImageURI(Uri.fromFile(foto));

            Glide.with(context)
                    .load(entrevistas.getImagen())
                    .apply(new RequestOptions().override(72, 86)) // Opcional: ajusta el tamaño de la imagen si es necesario
                    .into(imageView);
        }
    }

    public void setFilteredList(List<Entrevistas> filteredList) {
        this.datos = filteredList;
        notifyDataSetChanged();
    }
}


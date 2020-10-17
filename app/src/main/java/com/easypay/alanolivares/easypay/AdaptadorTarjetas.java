package com.easypay.alanolivares.easypay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorTarjetas extends ArrayAdapter<TarjetaObjeto> {
    private ArrayList<TarjetaObjeto> lista_tarjetas;
    private Context context;

    public AdaptadorTarjetas(Context context, ArrayList<TarjetaObjeto> lista){
        super(context,R.layout.vista_tarjetas,lista);
        this.lista_tarjetas = lista;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null){
            LayoutInflater vi = LayoutInflater.from(context);
            convertView =vi.inflate(R.layout.vista_tarjetas,null);
        }

        TarjetaObjeto contacto=lista_tarjetas.get(position);
        if(contacto!=null){
            TextView txtNombre = (TextView) convertView.findViewById(R.id.tarjeta);
            ImageView imgFoto =(ImageView) convertView.findViewById(R.id.imgFoto);
            if(contacto.tarjeta.charAt(0)=='4'){
                imgFoto.setImageResource(R.drawable.visa);
            }else if(contacto.tarjeta.charAt(0)=='5'){
                imgFoto.setImageResource(R.drawable.mastercard);
            }
            txtNombre.setText("*****"+contacto.tarjeta.substring(contacto.tarjeta.length()-4,contacto.tarjeta.length()));
            Button borrar = convertView.findViewById(R.id.borrar);
            borrar.setTag(position);
            final View finalConvertView = convertView;
            borrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(finalConvertView, position, 0);
                }
            });
            txtNombre.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(finalConvertView, position, 1);
                }
            });

            //System.out.println(contacto.getNombre());
            //imgFoto.setImageResource(contacto.getImagen());



        }
        return  convertView;
    }


}


package com.easypay.alanolivares.easypay;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AdaptadorHistorial extends ArrayAdapter<HistoriaObjeto> {
    private ArrayList<HistoriaObjeto> lista;
    private Context context;

    public AdaptadorHistorial(Context context, ArrayList<HistoriaObjeto> lista){
        super(context,R.layout.vista_historial,lista);
        this.lista = lista;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null){
            LayoutInflater vi = LayoutInflater.from(context);
            convertView =vi.inflate(R.layout.vista_historial,null);
        }

        HistoriaObjeto contacto=lista.get(position);
        if(contacto!=null){

            TextView boleto = (TextView) convertView.findViewById(R.id.IDBol);
            TextView fecha = (TextView) convertView.findViewById(R.id.fechaBol);
            TextView conductor = (TextView) convertView.findViewById(R.id.ConductorBol);
            TextView ruta = (TextView) convertView.findViewById(R.id.RutaBol);
            TextView costo = (TextView) convertView.findViewById(R.id.costoBol);
            boleto.setText(contacto.getBoleto());
            fecha.setText(contacto.getFecha());
            conductor.setText(contacto.getConductor());
            ruta.setText(contacto.getRuta());
            costo.setText("$"+contacto.getCosto());

            final View finalConvertView = convertView;
            Button problema = convertView.findViewById(R.id.problemaBol);


            problema.setTag(position);
            problema.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(finalConvertView, position, 0);
                }
            });




        }
        return  convertView;
    }


}
class HistoriaObjeto{
    String boleto;
    String fecha;
    String conductor;
    String ruta;
    String costo;

    public HistoriaObjeto(String boleto, String fecha, String conductor, String ruta,String costo) {
        this.boleto = boleto;
        this.fecha = fecha;
        this.conductor = conductor;
        this.ruta = ruta;
        this.costo=costo;
    }

    public String getCosto() {
        return costo;
    }

    public void setCosto(String costo) {
        this.costo = costo;
    }

    public String getBoleto() {
        return boleto;
    }

    public void setBoleto(String boleto) {
        this.boleto = boleto;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getConductor() {
        return conductor;
    }

    public void setConductor(String conductor) {
        this.conductor = conductor;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}

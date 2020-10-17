package com.easypay.alanolivares.easypay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class Historial extends Fragment {
    private ListView listView;
    private AdaptadorHistorial adaptador;
    ArrayList<HistoriaObjeto> lista;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view=inflater.inflate(R.layout.fragment_historial, container, false);
        listView=(ListView)view.findViewById(R.id.listViewCapitulos);
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios", Context.MODE_PRIVATE);
        final String correo = preferences.getString("correo","No existe");
        setHasOptionsMenu(true);
        new ActualizarDatos().execute(correo);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                int opc=(int) id;
                switch (opc){
                    case 0:
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("Confirmación").
                                setMessage("Estas seguro de abrir una queja para el boleto con id:  "+lista.get(position).getBoleto()).
                                setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent re = new Intent(getActivity(), Quejas.class);
                                        re.putExtra("boleto",lista.get(position).boleto);
                                        re.putExtra("conductor",lista.get(position).conductor);
                                        re.putExtra("fecha",lista.get(position).fecha);
                                        startActivity(re);
                                    }
                                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;


                }

            }
        });
        // Inflate the layout for this fragment
        return view;
    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.historial, menu);

    }



    private class ActualizarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                //"http://192.168.100.22/easypay/historial.php?correo="+
                URL url3 = new URL("http://10.10.26.195/easypay/historial.php?correo="+urls[0]);
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(url3.openStream()));
                String response = new String();
                for (String line; (line = reader1.readLine()) != null; response += line);
                return response;
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            String boleto,fecha,costo,nombreC,apellidoC,ruta,numeco;
            lista=new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i=0; i<jsonArray.length(); i++) {
                    JSONObject jsonObject= jsonArray.getJSONObject(i);
                    boleto=jsonObject.getString("idboleto");
                    fecha=jsonObject.getString("fecha");
                    costo=jsonObject.getString("costo");
                    nombreC=jsonObject.getString("nombre");
                    apellidoC=jsonObject.getString("apellido");
                    ruta=jsonObject.getString("ruta");
                    numeco=jsonObject.getString("numeroeco");
                    lista.add(new HistoriaObjeto(boleto,fecha,nombreC+" "+apellidoC,ruta+"-"+numeco,costo));
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
            AdaptadorHistorial adaptador = new AdaptadorHistorial(getContext(),lista);
            listView.setAdapter(adaptador);
            if(lista.isEmpty())
                Snackbar
                        .make(getActivity().findViewById(android.R.id.content), "Lista de viajes vacia",Snackbar.LENGTH_LONG)
                        .show();

        }
    }

}

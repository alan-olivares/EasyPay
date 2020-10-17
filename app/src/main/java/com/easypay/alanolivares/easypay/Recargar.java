package com.easypay.alanolivares.easypay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class Recargar extends Fragment {
    Button pagar,paypal;
    EditText tarjeta,mes,ano,ccv,monto;
    TextView saldo;
    ArrayList<TarjetaObjeto> lista_tarjeta;
    ArrayAdapter<TarjetaObjeto> adapter;
    private ListView listViewtarjetas;
    private static int save = -1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view= inflater.inflate(R.layout.fragment_recargar,container,false);
        view.clearFocus();
        setHasOptionsMenu(true);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        final String correo = preferences.getString("correo","No existe");
        setHasOptionsMenu(true);
        pagar=view.findViewById(R.id.pagar);
        monto=view.findViewById(R.id.monto);
        saldo=view.findViewById(R.id.saldo2);
        listViewtarjetas = (ListView)view.findViewById(R.id.listTarjetas);
        new ConsultarDatos().execute("http://10.10.26.195/easypay/consulta.php?correo="+correo);
        cargar();
        pagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(save==-1){
                    Toast.makeText(getContext(), "Ninguna tarjeta seleccionada", Toast.LENGTH_LONG).show();
                }else{
                    if(monto.getText().toString().isEmpty()){
                        Toast.makeText(getContext(), "Ingresa el monto a recargar", Toast.LENGTH_LONG).show();
                    }else{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("Confirmación").
                                setMessage("Estas seguro de pagar "+monto.getText().toString()+" pesos con la tarjeta terminación "+lista_tarjeta.get(save).tarjeta.substring(lista_tarjeta.get(save).tarjeta.length()-4,lista_tarjeta.get(save).tarjeta.length())).
                                setPositiveButton("Pagar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new ActualizarDatos().execute("http://10.10.26.195/easypay/saldo.php?correo="+correo+"&saldo="+monto.getText().toString());
                                        Toast.makeText(getContext(), "Pago exitoso!!", Toast.LENGTH_LONG).show();

                                    }
                                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        });
        listViewtarjetas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = (int)id;
                Toast.makeText(getContext(),"Boton borrar: "+position,Toast.LENGTH_LONG);

                switch (viewId){
                    case 0:
                        //Boton Detalles
                        eliminar(position);
                        System.out.println("Algo");
                        Toast.makeText(getActivity(),"Boton borrar: "+position,Toast.LENGTH_LONG);
                        break;
                    case 1:
                        parent.getChildAt(position).setBackgroundColor(Color.parseColor("#A4A4A4"));

                        if (save != -1 && save != position){
                            parent.getChildAt(save).setBackgroundColor(Color.TRANSPARENT);
                        }

                        save = position;
                        System.out.println("Algo33");
                        break;
                    default:

                        //item de la lista
                        Toast.makeText(getContext(),"Item lista: "+position,Toast.LENGTH_LONG);
                        break;
                }
            }
        });
        return view;
    }


    public void eliminar(int pos){
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaTarjetas","No existe");
        Type type = new TypeToken<ArrayList<TarjetaObjeto>>(){}.getType();
        ArrayList<TarjetaObjeto> listacaheTarjetas = gson.fromJson(savedList, type);
        lista_tarjeta=listacaheTarjetas;
        lista_tarjeta.remove(pos);
        String jsonList = gson.toJson(lista_tarjeta);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putString("listaTarjetas",jsonList);
        editor.commit();
        cargar();
    }
    public void cargar(){
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaTarjetas","No existe");
        if(!savedList.equals("No existe")){
            Type type = new TypeToken<ArrayList<TarjetaObjeto>>(){}.getType();
            ArrayList<TarjetaObjeto> listacaheTarjetas = gson.fromJson(savedList, type);
            lista_tarjeta=new ArrayList<>();
            lista_tarjeta=listacaheTarjetas;
            AdaptadorTarjetas adaptador = new AdaptadorTarjetas(getContext(),lista_tarjeta);
            listViewtarjetas.setAdapter(adaptador);
        }else{
            Snackbar
                    .make(getActivity().findViewById(android.R.id.content), "Ninguna tarjeta registrada",Snackbar.LENGTH_LONG)
                    .show();

        }
    }
    @Override
    public void onResume(){
        super.onResume();
        cargar();
        try{
            if(dialog.isShowing()){
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    AlertDialog dialog;
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.aplicacion, menu);
        MenuItem agregar=menu.findItem(R.id.agregar);
        agregar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.agregar_metodo_de_pago, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                        setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setTitle("Escoge tu opción");

                dialog = builder.create();
                if(dialog.isShowing()){
                    dialog.dismiss();
                }
                dialog.setView(dialogLayout);
                dialog.show();
                // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                ImageButton tarjeta=dialog.findViewById(R.id.agregartarjeta);
                tarjeta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent re = new Intent(getContext(), AgregarTarjeta.class);
                        startActivity(re);
                    }
                });
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.copyFrom(dialog.getWindow().getAttributes());
                dialog.getWindow().setAttributes(lp);


                return false;
            }
        });
    }

    private class ConsultarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            String sal;
            JSONArray ja = null;
            try {
                System.out.println(result);
                ja = new JSONArray(result);
                sal=ja.getString(2);
                saldo.setText(sal);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    private class ActualizarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            saldo.setText(result);

        }
    }

    private String downloadUrl(String myurl) throws IOException {
        Log.i("URL",""+myurl);
        myurl = myurl.replace(" ","%20");
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("respuesta", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


}

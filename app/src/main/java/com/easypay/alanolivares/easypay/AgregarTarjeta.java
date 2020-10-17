package com.easypay.alanolivares.easypay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AgregarTarjeta extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Button pagar,paypal;
    EditText tarjeta,mes,ano,ccv,monto,titular;
    ArrayList<TarjetaObjeto> lista_tarjeta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tarjeta);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tarjeta=findViewById(R.id.ntarjeta);
        mes=findViewById(R.id.mes);
        ano=findViewById(R.id.ano);
        ccv=findViewById(R.id.ccv);
        monto=findViewById(R.id.monto);
        titular=findViewById(R.id.titular);

    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflar el menú; Esto agrega elementos a la barra de acción si está presente.
        getMenuInflater().inflate(R.menu.guardar, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // El elemento de la barra de acción de la manija hace clic aquí. La barra de acción
        // controlará automáticamente los clics en el botón Inicio / Arriba,
        // siempre y cuando especifique una actividad principal en AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.save) {
            vacio();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void vacio(){
        tarjeta.setError(null);
        mes.setError(null);
        ano.setError(null);
        ccv.setError(null);
        titular.setError(null);
        String tar=tarjeta.getText().toString();
        String me=mes.getText().toString();
        String an=ano.getText().toString();
        String cc=ccv.getText().toString();
        String titu=titular.getText().toString();
        boolean cancel=false;
        View focusView = null;
        if (TextUtils.isEmpty(tar) || !tarjeta(tar)) {
            tarjeta.setError("Tarjeta Incorrecta");
            focusView = tarjeta;
            cancel = true;
        }
        if (TextUtils.isEmpty(titu)) {
            titular.setError("Nombre incorrecto");
            focusView = titular;
            cancel = true;
        }
        if (TextUtils.isEmpty(me) || !mes(me)) {
            mes.setError("Mes incorrecto");
            focusView = mes;
            cancel = true;
        }
        if (TextUtils.isEmpty(an) || !ano(an)) {
            ano.setError("Año incorrecto");
            focusView = ano;
            cancel = true;
        }
        if (TextUtils.isEmpty(cc) || !ccv(cc)) {
            ccv.setError("CCV incorrecto");
            focusView = ccv;
            cancel = true;
        }
        if (cancel) {
            // Hubo un error; No intente iniciar sesión
            // y enfocar el primer campo de formulario con un error.
            focusView.requestFocus();
        } else {
            guardarTarjeta(tar, me, an,cc,titu);

            // Mostrar un hilandero de progreso y iniciar una tarea
            // en segundo plano para realizar el intento de inicio de sesión de usuario.

        }

    }
    public void guardarTarjeta(String tar,String me,String an,String cc,String titu){
        SharedPreferences preferences = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaTarjetas","No existe");
        if(!savedList.equals("No existe")){
            Type type = new TypeToken<ArrayList<TarjetaObjeto>>(){}.getType();
            ArrayList<TarjetaObjeto> listacaheTarjetas = gson.fromJson(savedList, type);
            lista_tarjeta=listacaheTarjetas;
            lista_tarjeta.add(new TarjetaObjeto(tar, me, an,cc,titu));
            String jsonList = gson.toJson(lista_tarjeta);
            SharedPreferences.Editor editor = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaTarjetas",jsonList);
            editor.commit();
        }else{
            lista_tarjeta=new ArrayList<>();
            lista_tarjeta.add(new TarjetaObjeto(tar, me, an,cc,titu));
            String jsonList = gson.toJson(lista_tarjeta);
            SharedPreferences.Editor editor = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaTarjetas",jsonList);
            editor.commit();
        }
        Toast.makeText(this, "Tarjeta Guardada", Toast.LENGTH_LONG).show();
        onBackPressed();

    }
    private boolean tarjeta(String tarjeta) {
        if(tarjeta.length()==16){
            return true;
        }else{
            return false;
        }

    }
    private boolean ccv(String ccv) {
        if(ccv.length()==3){
            return true;
        }else{
            return false;
        }
    }
    private boolean mes(String mes) {
        if(mes.length()==2){
            return true;
        }else{
            return false;
        }
    }
    private boolean ano(String ano) {
        if(ano.length()==2){
            return true;
        }else{
            return false;
        }
    }
}

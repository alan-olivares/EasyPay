package com.easypay.alanolivares.easypay;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Pago extends Fragment {
    ImageButton qr,nfc;
    String correo,contra;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    TextView sal;
    public Handler handler = new Handler();
    Tag myTag;
    Boolean disponfc;
    Context context;
    String qrgen;
    TextView dis;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view;
        view= inflater.inflate(R.layout.fragment_pago,container,false);
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        final String correo = preferences.getString("correo","No existe");
        final String contra = preferences.getString("contra","No existe");
        new ConsultarDatos().execute("http://10.10.26.195/easypay/consulta.php?correo="+correo);
        qr=view.findViewById(R.id.imageqr);
        nfc=view.findViewById(R.id.imagenfc);
        dis=view.findViewById(R.id.disponible);
        sal=view.findViewById(R.id.saldo);
        setHasOptionsMenu(true);
        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrgen=correo+"::"+contra;
                MultiFormatWriter multiFormatWriter= new MultiFormatWriter();
                try{
                    BitMatrix bitMatrix =multiFormatWriter.encode(qrgen,BarcodeFormat.QR_CODE,800,800);
                    BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
                    Bitmap bitmap= barcodeEncoder.createBitmap(bitMatrix);
                    ImageView image = new ImageView(getContext());
                    image.setImageBitmap(bitmap);

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(getContext()).
                                    setMessage("Muestra este código al lector QR").
                                    setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).
                                    setView(image);
                    builder.create().show();
                }catch (WriterException e){
                    e.printStackTrace();
                }
            }
        });
        nfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.video, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                        setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setMessage("Coloca tu celular sobre el verificador NFC");

                final AlertDialog dialog = builder.create();
                dialog.setView(dialogLayout);
                dialog.show();
                // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                final VideoView video_player_view = (VideoView) dialog.findViewById(R.id.videoView);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.copyFrom(dialog.getWindow().getAttributes());
                dialog.getWindow().setAttributes(lp);
                Uri uri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.nf);
                video_player_view.setVideoURI(uri);
                video_player_view.start();
                video_player_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        video_player_view.start();
                    }
                });
                try{
                    //Si no existe tag al que escribir, mostramos un mensaje de que no existe.
                    if(myTag == null){
                        Toast.makeText(getContext(), "Error al pagar", Toast.LENGTH_LONG).show();


                    }else{
                        //Llamamos al método write que definimos más adelante donde le pasamos por
                        //parámetro el tag que hemos detectado y el mensaje a escribir.
                        write(qrgen,myTag);
                        handler.postDelayed(
                                new Runnable() {
                                    public void run() {
                                        dialog.dismiss();
                                        Snackbar
                                                .make(getActivity().findViewById(android.R.id.content), "¡Pago realizado con exito!",Snackbar.LENGTH_LONG)
                                                .show();
                                    }},
                                8000);

                    }
                }catch(IOException e){
                    Toast.makeText(getContext(), "Error al escribir",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }catch(FormatException e){
                    Toast.makeText(getContext(), "Error al escribir", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        adapter = NfcAdapter.getDefaultAdapter(getContext());
        if (adapter == null) {
            // Stop here, we definitely need NFC
            dis.setText("Pago con NFC no disponible");
            dis.setTextColor(getResources().getColor(R.color.rojo));
            nfc.setClickable(false);
            nfc.getBackground().setAlpha(120);
            disponfc=false;
            Toast.makeText(getContext(), "NFC no soportado", Toast.LENGTH_LONG).show();

        }else{
            pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(),getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
            writeTagFilters = new IntentFilter[]{tagDetected};
            dis.setText("Pago con NFC disponible");
        }

        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.opciones, menu);

    }


    //El método write es el más importante, será el que se encargue de crear el mensaje
    //y escribirlo en nuestro tag.
    private void write(String text, Tag tag) throws IOException, FormatException{
        //Creamos un array de elementos NdefRecord. Este Objeto representa un registro del mensaje NDEF
        //Para crear el objeto NdefRecord usamos el método createRecord(String s)
        NdefRecord[] records = {createRecord(text)};
        //NdefMessage encapsula un mensaje Ndef(NFC Data Exchange Format). Estos mensajes están
        //compuestos por varios registros encapsulados por la clase NdefRecord
        NdefMessage message = new NdefMessage(records);
        //Obtenemos una instancia de Ndef del Tag
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }
    //Método createRecord será el que nos codifique el mensaje para crear un NdefRecord
    @SuppressLint("NewApi") private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang = "us";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payLoad = new byte[1 + langLength + textLength];

        payLoad[0] = (byte) langLength;

        System.arraycopy(langBytes, 0, payLoad, 1, langLength);
        System.arraycopy(textBytes, 0, payLoad, 1+langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payLoad);

        return recordNFC;

    }
    //en onnewIntent manejamos el intent para encontrar el Tag
    @SuppressLint("NewApi") protected void onNewIntent(Intent intent){
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(getContext(), myTag.toString(), Toast.LENGTH_LONG).show();

        }
    }

    public void onPause(){
        super.onPause();
        adapter = NfcAdapter.getDefaultAdapter(getContext());
        if(adapter!=null){
            WriteModeOff();
        }
    }
    public void onResume(){
        super.onResume();
        adapter = NfcAdapter.getDefaultAdapter(getContext());
        if(adapter!=null){
            WriteModeOn();
        }
    }

    @SuppressLint("NewApi") private void WriteModeOn(){
        writeMode = true;
        adapter.enableForegroundDispatch(getActivity(), pendingIntent, writeTagFilters, null);
    }

    @SuppressLint("NewApi") private void WriteModeOff(){
        writeMode = false;
        adapter.disableForegroundDispatch(getActivity());
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
            String saldo;
            JSONArray ja = null;
            try {
                System.out.println(result);
                ja = new JSONArray(result);
                saldo=ja.getString(2);
                sal.setText(saldo);

            } catch (JSONException e) {
                e.printStackTrace();
            }

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



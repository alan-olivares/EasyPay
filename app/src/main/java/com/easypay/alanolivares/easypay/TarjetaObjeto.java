package com.easypay.alanolivares.easypay;

import android.os.Parcel;
import android.os.Parcelable;

public class TarjetaObjeto implements Parcelable {
    String tarjeta;
    String imagen;
    String mes;
    String ano;
    String ccv;
    String titular;
    public TarjetaObjeto(String tarjeta,String mes,String ano,String ccv,String titular){
        this.tarjeta= tarjeta;
        this.mes=mes;
        this.ano=ano;
        this.ccv=ccv;
        this.titular=titular;
    }

    public String getNombre() {
        return tarjeta;
    }

    public void setNombre(String nombre) {
        this.tarjeta = nombre;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    protected TarjetaObjeto(Parcel in) {
        tarjeta = in.readString();
        imagen = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tarjeta);
        dest.writeString(imagen);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TarjetaObjeto> CREATOR = new Parcelable.Creator<TarjetaObjeto>() {
        @Override
        public TarjetaObjeto createFromParcel(Parcel in) {
            return new TarjetaObjeto(in);
        }

        @Override
        public TarjetaObjeto[] newArray(int size) {
            return new TarjetaObjeto[size];
        }
    };
}




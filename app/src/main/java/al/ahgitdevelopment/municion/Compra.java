package al.ahgitdevelopment.municion;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alberto on 12/05/2016.
 */
public class Compra implements Parcelable {
    public static final Creator<Compra> CREATOR = new Creator<Compra>() {
        @Override
        public Compra createFromParcel(Parcel in) {
            return new Compra(in);
        }

        @Override
        public Compra[] newArray(int size) {
            return new Compra[size];
        }
    };
    private int id;
    private String calibre1;
    private String calibre2;
    private int unidades;
    private double precio;
    private Date fecha;
    private int tipo;
    private String peso;
    private String marca;
    private String tienda;
    private double valoracion;
    private Bitmap imagen;

    protected Compra() {
    }

    protected Compra(Parcel in) {
        id = in.readInt();
        calibre1 = in.readString();
        calibre2 = in.readString();
        unidades = in.readInt();
        precio = in.readDouble();
        tipo = in.readInt();
        peso = in.readString();
        marca = in.readString();
        tienda = in.readString();
    }

    public Compra(Bundle extras) {
        calibre1 = extras.getString("calibre1");
        calibre2 = extras.getString("calibre2", "");
        unidades = extras.getInt("unidades");
        precio = extras.getDouble("precio");
        try {
            fecha = new SimpleDateFormat("dd/MM/yyyy").parse(extras.getString("fecha", ""));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tipo = extras.getInt("tipo");
        peso = extras.getString("peso");
        marca = extras.getString("marca");
        tienda = extras.getString("tienda");
        valoracion = extras.getInt("valoracion");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCalibre1() {
        return calibre1;
    }

    public void setCalibre1(String calibre1) {
        this.calibre1 = calibre1;
    }

    public String getCalibre2() {
        return calibre2;
    }

    public void setCalibre2(String calibre2) {
        this.calibre2 = calibre2;
    }

    public int getUnidades() {
        return unidades;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setFecha(String fecha) {
        try {
            this.fecha = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getTienda() {
        return tienda;
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }

    public double getValoracion() {
        return valoracion;
    }

    public void setValoracion(double valoracion) {
        this.valoracion = valoracion;
    }

    public Bitmap getImagen() {
        return imagen;
    }

    public void setImagen(Bitmap imagen) {
        this.imagen = imagen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(calibre1);
        dest.writeString(calibre2);
        dest.writeInt(unidades);
        dest.writeDouble(precio);
        dest.writeInt(tipo);
        dest.writeString(peso);
        dest.writeString(marca);
        dest.writeString(tienda);
    }
}

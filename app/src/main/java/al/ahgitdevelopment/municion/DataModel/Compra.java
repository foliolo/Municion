package al.ahgitdevelopment.municion.DataModel;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    private int idPosGuia;
    private String calibre1;
    private String calibre2;
    private int unidades;
    private double precio;
    private Calendar fecha;
    private String tipo;
    private int peso;
    private String marca;
    private String tienda;
    private float valoracion;
    private String imagePath;

    public Compra() {
    }

    protected Compra(Parcel in) {
        id = in.readInt();
        idPosGuia = in.readInt();
        calibre1 = in.readString();
        calibre2 = in.readString();
        unidades = in.readInt();
        precio = in.readDouble();
        try {
            this.setFecha(in.readString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        tipo = in.readString();
        peso = in.readInt();
        marca = in.readString();
        tienda = in.readString();
        valoracion = in.readFloat();
        imagePath = in.readString();
    }

    public Compra(Bundle extras) {
        idPosGuia = extras.getInt("idPosGuia");
        calibre1 = extras.getString("calibre1");
        calibre2 = extras.getString("calibre2", "");
        unidades = extras.getInt("unidades");
        precio = extras.getDouble("precio");
        this.setFecha(extras.getString("fecha", ""));
        tipo = extras.getString("tipo");
        peso = extras.getInt("peso");
        marca = extras.getString("marca");
        tienda = extras.getString("tienda");
        valoracion = extras.getFloat("valoracion");
        imagePath = extras.getString("imagePath");
    }

    public int getIdPosGuia() {
        return idPosGuia;
    }

    public void setIdPosGuia(int idPosGuia) {
        this.idPosGuia = idPosGuia;
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

    public Calendar getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        Calendar auxFecha = Calendar.getInstance();
        try {
            auxFecha.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(fecha));
            if (this.fecha == null)
                this.fecha = Calendar.getInstance();
            this.fecha = auxFecha;
        } catch (ParseException e) {
            e.printStackTrace();
            this.fecha = auxFecha;
        }
    }

    public void setFecha(Calendar fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
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

    public float getValoracion() {
        return valoracion;
    }

    public void setValoracion(float valoracion) {
        this.valoracion = valoracion;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idPosGuia);
        dest.writeInt(id);
        dest.writeString(calibre1);
        dest.writeString(calibre2);
        dest.writeInt(unidades);
        dest.writeDouble(precio);
        try {
            dest.writeString(new SimpleDateFormat("dd/MM/yyyy").format(getFecha().getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dest.writeString(tipo);
        dest.writeInt(peso);
        dest.writeString(marca);
        dest.writeString(tienda);
        dest.writeFloat(valoracion);
        dest.writeString(imagePath);
    }
}

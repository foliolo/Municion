package al.ahgitdevelopment.municion.datamodel;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alberto on 13/05/2016.
 */
public class Guia implements Parcelable {
    public static final Creator<Guia> CREATOR = new Creator<Guia>() {
        @Override
        public Guia createFromParcel(Parcel in) {
            return new Guia(in);
        }

        @Override
        public Guia[] newArray(int size) {
            return new Guia[size];
        }
    };
    private int id;
    private int idCompra;
    private int tipoLicencia;
    private String marca;
    private String modelo;
    private String apodo;
    private int tipoArma;
    private String calibre1;
    private String calibre2;
    private String numGuia;
    private String numArma;
    private String imagePath;
    private int cupo;
    private int gastado;

    public Guia() {
    }

    protected Guia(Parcel in) {
        id = in.readInt();
        idCompra = in.readInt();
        tipoLicencia = in.readInt();
        marca = in.readString();
        modelo = in.readString();
        apodo = in.readString();
        tipoArma = in.readInt();
        calibre1 = in.readString();
        calibre2 = in.readString();
        numGuia = in.readString();
        numArma = in.readString();
        imagePath = in.readString();
        cupo = in.readInt();
        gastado = in.readInt();
    }

    public Guia(Bundle extras) {
        tipoLicencia = extras.getInt("tipoLicencia");
        marca = extras.getString("marca");
        modelo = extras.getString("modelo");
        apodo = extras.getString("apodo");
        tipoArma = extras.getInt("tipoArma");
        calibre1 = extras.getString("calibre1");
        calibre2 = extras.getString("calibre2");
        numGuia = extras.getString("numGuia");
        numArma = extras.getString("numArma");
        imagePath = extras.getString("imagePath");
        gastado = extras.getInt("gastado");
        cupo = extras.getInt("cupo");
        imagePath = extras.getString("imagePath");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getTipoLicencia() {
        return tipoLicencia;
    }

    public void setTipoLicencia(int tipoLicencia) {
        this.tipoLicencia = tipoLicencia;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getApodo() {
        return apodo;
    }

    public void setApodo(String apodo) {
        this.apodo = apodo;
    }

    public int getTipoArma() {
        return tipoArma;
    }

    public void setTipoArma(int tipoArma) {
        this.tipoArma = tipoArma;
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

    public String getNumGuia() {
        return numGuia;
    }

    public void setNumGuia(String numGuia) {
        this.numGuia = numGuia;
    }

    public String getNumArma() {
        return numArma;
    }

    public void setNumArma(String numArma) {
        this.numArma = numArma;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getCupo() {
        return cupo;
    }

    public void setCupo(int cupo) {
        this.cupo = cupo;
    }

    public int getGastado() {
        return gastado;
    }

    public void setGastado(int gastado) {
        this.gastado = gastado;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(idCompra);
        dest.writeInt(tipoLicencia);
        dest.writeString(marca);
        dest.writeString(modelo);
        dest.writeString(apodo);
        dest.writeInt(tipoArma);
        dest.writeString(calibre1);
        dest.writeString(calibre2);
        dest.writeString(numGuia);
        dest.writeString(numArma);
        dest.writeString(imagePath);
        dest.writeInt(cupo);
        dest.writeInt(gastado);
    }
}
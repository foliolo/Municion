package al.ahgitdevelopment.municion;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Alberto on 13/05/2016.
 */
public class Licencia implements Parcelable {
    public static final Creator<Licencia> CREATOR = new Creator<Licencia>() {
        @Override
        public Licencia createFromParcel(Parcel in) {
            return new Licencia(in);
        }

        @Override
        public Licencia[] newArray(int size) {
            return new Licencia[size];
        }
    };
    private int id;
    private String tipo;
    private int numLicencia;
    private Date fechaExpedicion;
    private Date fechaCaducidad;

    protected Licencia() {
    }

    protected Licencia(Parcel in) {
        id = in.readInt();
        tipo = in.readString();
        numLicencia = in.readInt();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getNumLicencia() {
        return numLicencia;
    }

    public void setNumLicencia(int numLicencia) {
        this.numLicencia = numLicencia;
    }

    public Date getFechaExpedicion() {
        return fechaExpedicion;
    }

    public void setFechaExpedicion(Date fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(tipo);
        dest.writeInt(numLicencia);
    }
}

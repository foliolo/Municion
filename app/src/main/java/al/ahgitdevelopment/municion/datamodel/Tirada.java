package al.ahgitdevelopment.municion.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

import al.ahgitdevelopment.municion.Utils;

/**
 * Created by ahidalgog on 12/01/2017.
 */

public class Tirada implements Parcelable {

    public static final Creator<Tirada> CREATOR = new Creator<Tirada>() {
        @Override
        public Tirada createFromParcel(Parcel in) {
            return new Tirada(in);
        }

        @Override
        public Tirada[] newArray(int size) {
            return new Tirada[size];
        }
    };
    /**
     * Descripción
     */
    private String descripcion;
    /**
     * Rango de la tirada [nacional, autonomica, local/social]
     */
    private String rango;
    /**
     * Fecha de la tirada
     */
    private String fecha;
    /**
     * Puntuación conseguida en la tirada [0-600]
     */
    private int puntuacion;

    public Tirada() {
        this.descripcion = "";
        this.rango = "";
        this.fecha = "";
        this.puntuacion = 0;
    }

    public Tirada(Parcel in) {
        descripcion = in.readString();
        rango = in.readString();
        fecha = in.readString();
        puntuacion = in.readInt();
    }

    public Tirada(Tirada tirada) {
        this.descripcion = tirada.descripcion;
        this.rango = tirada.rango;
        this.fecha = tirada.fecha;
        this.puntuacion = tirada.puntuacion;
    }

    /**
     * Método que obtiene los milisegundos que quedan desde la fecha actual hasta un años despues de la ultima tirada
     *
     * @param tirada Array de tiaradas realizadas
     * @return Milisegundos hasta que expire la licencia
     */
    public static long millisUntilExpiracy(Tirada tirada) {
        Calendar ultimaTirada = Calendar.getInstance();
        ultimaTirada.setTime(Utils.getDateFromString(tirada.getFecha()));

        Calendar expiracy = Calendar.getInstance();
        expiracy.setTime(ultimaTirada.getTime());
        expiracy.set(Calendar.YEAR, expiracy.get(Calendar.YEAR) + 1);
        return expiracy.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRango() {
        return rango;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(descripcion);
        dest.writeString(rango);
        dest.writeString(fecha);
        dest.writeInt(puntuacion);
    }
}

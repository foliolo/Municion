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
     * Localización de la tirada [lugar/galería de tiro]
     * NOTA: Se mantiene el nombre interno para compatibilidad con Parcelable legacy
     */
    private String localizacion;
    /**
     * Categoría de la tirada [Nacional, Autonómica, Local/Social]
     */
    private String categoria;
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
        this.localizacion = "";
        this.categoria = "";
        this.fecha = "";
        this.puntuacion = 0;
    }

    public Tirada(Parcel in) {
        descripcion = in.readString();
        localizacion = in.readString();
        categoria = in.readString();
        fecha = in.readString();
        puntuacion = in.readInt();
    }

    public Tirada(Tirada tirada) {
        this.descripcion = tirada.descripcion;
        this.localizacion = tirada.localizacion;
        this.categoria = tirada.categoria;
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

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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
        dest.writeString(localizacion);
        dest.writeString(categoria);
        dest.writeString(fecha);
        dest.writeInt(puntuacion);
    }
}

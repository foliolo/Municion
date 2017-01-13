package al.ahgitdevelopment.municion.DataModel;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
     * Método que obtiene la fecha de la ultima tirada realizada
     *
     * @param Lista de tiaradas realizadas
     * @return Fecha de la ultima tirada
     */
    public static String ultimaTiradaRealizada(@NonNull ArrayList<Tirada> tiradas) {
        Calendar auxLastDate = Calendar.getInstance();
        Calendar expiracy = Calendar.getInstance();
        auxLastDate.set(2000, 1, 1); //Iniciamos la fecha en el año 2000.

        for (Tirada tirada : tiradas) {
            try {
                Calendar fechaTirada = Calendar.getInstance();
                fechaTirada.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(tirada.getFecha()));
                //Actualizamos auxLastDate si fechaTirada es mas actual
                if (fechaTirada.compareTo(auxLastDate) > 0) {
                    auxLastDate = fechaTirada;
                    expiracy.set(Calendar.YEAR, auxLastDate.get(Calendar.YEAR) + 1);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        long daysInMillis = expiracy.getTimeInMillis() - auxLastDate.getTimeInMillis();
        return String.valueOf((daysInMillis / (1000 * 60 * 60 * 24)) / 7);

//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        return dateFormat.format(auxLastDate.getTime());
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

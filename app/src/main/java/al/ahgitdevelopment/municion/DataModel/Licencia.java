package al.ahgitdevelopment.municion.DataModel;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    private int tipo;
    private int numLicencia;
    private Calendar fechaExpedicion;
    private Calendar fechaCaducidad;

    public Licencia() {
    }

    protected Licencia(Parcel in) {
        this.id = in.readInt();
        this.tipo = in.readInt();
        this.numLicencia = in.readInt();
        try {
            this.setFechaExpedicion(in.readString());
            this.setFechaCaducidad(in.readString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public Licencia(Bundle extras) {
        this.tipo = extras.getInt("tipo");
        this.numLicencia = extras.getInt("num_licencia");
        this.setFechaExpedicion(extras.getString("fecha_expedicion", ""));
        this.setFechaCaducidad(extras.getString("fecha_caducidad", ""));
    }

    public Licencia(Licencia licencia) {
        this.setId(licencia.getId());
        this.setTipo(licencia.getTipo());
        this.setNumLicencia(licencia.getNumLicencia());
        this.setFechaExpedicion(licencia.getFechaExpedicion());
        this.setFechaCaducidad(licencia.getFechaCaducidad());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getNumLicencia() {
        return numLicencia;
    }

    public void setNumLicencia(int numLicencia) {
        this.numLicencia = numLicencia;
    }

    public Calendar getFechaExpedicion() {
        return fechaExpedicion;
    }

    public void setFechaExpedicion(Calendar fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }

    public void setFechaExpedicion(String fechaExpedicion) {
        Calendar fecha = Calendar.getInstance();
        try {
            fecha.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(fechaExpedicion));
            if (this.fechaExpedicion == null)
                this.fechaExpedicion = Calendar.getInstance();
            this.fechaExpedicion = fecha;
        } catch (ParseException e) {
            e.printStackTrace();
            this.fechaExpedicion = fecha;
        }
    }

    public Calendar getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Calendar fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidad) {
        Calendar fecha = Calendar.getInstance();
        try {
            fecha.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(fechaCaducidad));
            if (this.fechaCaducidad == null)
                this.fechaCaducidad = Calendar.getInstance();
            this.fechaCaducidad = fecha;
        } catch (ParseException e) {
            e.printStackTrace();
            this.fechaCaducidad = fecha;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(tipo);
        dest.writeInt(numLicencia);
        try {
            dest.writeString(new SimpleDateFormat("dd/MM/yyyy").format(getFechaExpedicion().getTime()));
            dest.writeString(new SimpleDateFormat("dd/MM/yyyy").format(getFechaCaducidad().getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

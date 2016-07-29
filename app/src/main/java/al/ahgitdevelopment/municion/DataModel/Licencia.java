package al.ahgitdevelopment.municion.DataModel;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import al.ahgitdevelopment.municion.R;

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
    private String nombre;
    private int tipoPermisoConduccion;
    private Calendar fechaExpedicion;
    private Calendar fechaCaducidad;
    private int numLicencia;
    private int numAbonado;
    private String numSeguro;
    private int autonomia;
    private int escala;

    public Licencia() {
    }

    protected Licencia(Parcel in) {
        this.id = in.readInt();
        this.tipo = in.readInt();
        this.nombre = in.readString();
        this.tipoPermisoConduccion = in.readInt();
        try {
            this.setFechaExpedicion(in.readString());
            this.setFechaCaducidad(in.readString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        this.numLicencia = in.readInt();
        this.numAbonado = in.readInt();
        this.numSeguro = in.readString();
        this.autonomia = in.readInt();
        this.escala = in.readInt();
    }

    public Licencia(Context context, Bundle extras) {
        this.tipo = extras.getInt("tipo");
        this.nombre = getNombre(context);
        this.tipoPermisoConduccion = extras.getInt("tipo_permiso_conduccion", -1);
        this.numLicencia = extras.getInt("num_licencia", -1);
        this.setFechaExpedicion(extras.getString("fecha_expedicion", ""));
        this.setFechaCaducidad(extras.getString("fecha_caducidad", ""));
        this.numAbonado = extras.getInt("num_abonado", -1);
        this.numSeguro = extras.getString("num_seguro", "");
        this.autonomia = extras.getInt("autonomia", -1);
        this.escala = extras.getInt("escala", -1);
    }

    public Licencia(Licencia licencia) {
        this.setId(licencia.getId());
        this.setTipo(licencia.getTipo());
        this.setNombre(licencia.getNombre());
        this.setNumLicencia(licencia.getNumLicencia());
        this.setFechaExpedicion(licencia.getFechaExpedicion());
        this.setFechaCaducidad(licencia.getFechaCaducidad());
        this.setNumAbonado(licencia.getNumAbonado());
        this.setNumSeguro(licencia.getNumSeguro());
        this.setAutonomia(licencia.getAutonomia());
        this.setEscala(licencia.getEscala());
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

    public String getNombre() {
        return nombre;
    }

    public String getNombre(Context context) {
        return context.getResources().getTextArray(R.array.tipo_licencias)[getTipo()].toString();
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTipoPermisoConduccion() {
        return tipoPermisoConduccion;
    }

    public void setTipoPermisoConduccion(int tipoPermisoConduccion) {
        this.tipoPermisoConduccion = tipoPermisoConduccion;
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

    public void setFechaExpedicion(Calendar fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }

    public int getNumAbonado() {
        return numAbonado;
    }

    public void setNumAbonado(int numAbonado) {
        this.numAbonado = numAbonado;
    }

    public int getAutonomia() {
        return autonomia;
    }

    public void setAutonomia(int autonomia) {
        this.autonomia = autonomia;
    }

    public String getNumSeguro() {
        return numSeguro;
    }

    public void setNumSeguro(String numSeguro) {
        this.numSeguro = numSeguro;
    }

    public Calendar getFechaCaducidad() {
        return fechaCaducidad;
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

    public void setFechaCaducidad(Calendar fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public int getEscala() {
        return escala;
    }

    public void setEscala(int escala) {
        this.escala = escala;
    }

    public String getStringEscala(Context context) {
        return context.getResources().getTextArray(R.array.tipo_escala)[getEscala()].toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(tipo);
        dest.writeString(nombre);
        dest.writeInt(tipoPermisoConduccion);
        try {
            dest.writeString(new SimpleDateFormat("dd/MM/yyyy").format(getFechaExpedicion().getTime()));
            dest.writeString(new SimpleDateFormat("dd/MM/yyyy").format(getFechaCaducidad().getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dest.writeInt(numLicencia);
        dest.writeInt(numAbonado);
        dest.writeString(numSeguro);
        dest.writeInt(autonomia);
        dest.writeInt(escala);
    }
}

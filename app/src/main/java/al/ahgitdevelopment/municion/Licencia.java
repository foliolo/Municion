package al.ahgitdevelopment.municion;

import java.util.Date;

/**
 * Created by Alberto on 13/05/2016.
 */
public class Licencia {
    private int id;
    private String tipo;
    private int num_licencia;
    private Date fechaExpedicion;
    private Date fechaCaducidad;

    public int getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getNum_licencia() {
        return num_licencia;
    }

    public void setNum_licencia(int num_licencia) {
        this.num_licencia = num_licencia;
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
}

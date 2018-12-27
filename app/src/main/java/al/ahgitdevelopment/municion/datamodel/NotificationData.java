package al.ahgitdevelopment.municion.datamodel;

import java.io.Serializable;

/**
 * Created by Alberto on 27/08/2016.
 */
public class NotificationData implements Serializable {

    private String licencia;
    private String id;
    private String fecha;

    public NotificationData() {
        this.licencia = "";
        this.id = "";
        this.fecha = "";
    }

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}

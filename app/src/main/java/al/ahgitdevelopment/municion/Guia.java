package al.ahgitdevelopment.municion;

import android.media.Image;

/**
 * Created by Alberto on 13/05/2016.
 */
public class Guia {
    private int id;
    private int idCompra;
    private int idLicencia;
    private String apodo;
    private String marca;
    private String modelo;
    private int tipoArma;
    private String calibre1;
    private String calibre2;
    private int numGuia;
    private int numAram;
    private Image imagen;
    private int cupo;
    private int gastado;

    public int getId() {
        return id;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdLicencia() {
        return idLicencia;
    }

    public void setIdLicencia(int idLicencia) {
        this.idLicencia = idLicencia;
    }

    public String getApodo() {
        return apodo;
    }

    public void setApodo(String apodo) {
        this.apodo = apodo;
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

    public int getNumGuia() {
        return numGuia;
    }

    public void setNumGuia(int numGuia) {
        this.numGuia = numGuia;
    }

    public int getNumAram() {
        return numAram;
    }

    public void setNumAram(int numAram) {
        this.numAram = numAram;
    }

    public Image getImagen() {
        return imagen;
    }

    public void setImagen(Image imagen) {
        this.imagen = imagen;
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
}

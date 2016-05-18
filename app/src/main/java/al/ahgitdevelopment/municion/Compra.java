package al.ahgitdevelopment.municion;

import java.util.Date;

/**
 * Created by Alberto on 12/05/2016.
 */
public class Compra {
    private int id;
    private String calibre1;
    private String calibre2;
    private String municionPropia;
    private int unidades;
    private double precio;
    private Date fecha;
    private int tipo;
    private String peso;
    private String marca;
    private String tiendoa;
    private Date valoracion;

    public int getId() {
        return id;
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

    public String getMunicionPropia() {
        return municionPropia;
    }

    public void setMunicionPropia(String municionPropia) {
        this.municionPropia = municionPropia;
    }

    public int getUnidades() {
        return unidades;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getTiendoa() {
        return tiendoa;
    }

    public void setTiendoa(String tiendoa) {
        this.tiendoa = tiendoa;
    }

    public Date getValoracion() {
        return valoracion;
    }

    public void setValoracion(Date valoracion) {
        this.valoracion = valoracion;
    }
}

package al.ahgitdevelopment.municion;

import android.os.Bundle;

/**
 * Created by Alberto on 11/04/2016.
 */
public class Compra {
    private int id;
    private String nombreArma;
    private float precio;
    private int cartuchosComprados;

    public Compra(Bundle bundle) {
        this.nombreArma = bundle.getString("nombreArma");
        this.precio = bundle.getFloat("precio", 0);
        this.cartuchosComprados = bundle.getInt("cartuchosComprados", 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreArma() {
        return nombreArma;
    }

    public void setNombreArma(String guia) {
        this.nombreArma = guia;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public int getCartuchosComprados() {
        return cartuchosComprados;
    }

    public void setCartuchosComprados(int cartuchosComprados) {
        this.cartuchosComprados = cartuchosComprados;
    }
}

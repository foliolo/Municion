package al.ahgitdevelopment.municion;

import android.os.Bundle;

/**
 * Created by Alberto on 29/03/2016.
 * Guia del arma, contiene la información ccompleta del arma.
 */
public class Guia {
    public String nombreArma;
    public String marca;
    public String modelo;
    public int numGuia;
    public String calibre;
    public String tipoArma;
    public int cartuchosGastados;
    public int cartuchosTotales;

    public Guia(Bundle bundle) {
        this.nombreArma = bundle.getString("nombreArma");
        this.marca = bundle.getString("marca");
        this.modelo = bundle.getString("modelo");
        this.numGuia = bundle.getInt("numGuia", -1);
        this.calibre = bundle.getString("calibre");
        this.tipoArma = bundle.getString("tipoArma");
        this.cartuchosGastados = bundle.getInt("cartuchosGastados", 0);
        this.cartuchosTotales = bundle.getInt("cartuchosTotales", 0);
    }

    public String getNombreArma() {
        return nombreArma;
    }

    public void setNombreArma(String nombreArma) {
        this.nombreArma = nombreArma;
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

    public int getNumGuia() {
        return numGuia;
    }

    public void setNumGuia(int numGuia) {
        this.numGuia = numGuia;
    }

    public String getCalibre() {
        return calibre;
    }

    public void setCalibre(String calibre) {
        this.calibre = calibre;
    }

    public String getTipoArma() {
        return tipoArma;
    }

    public void setTipoArma(String tipoArma) {
        this.tipoArma = tipoArma;
    }

    public int getCartuchosGastados() {
        return cartuchosGastados;
    }

    public void setCartuchosGastados(int cartuchosGastados) {
        this.cartuchosGastados = cartuchosGastados;
    }

    public int getCartuchosTotales() {
        return cartuchosTotales;
    }

    public void setCartuchosTotales(int cartuchosTotales) {
        this.cartuchosTotales = cartuchosTotales;
    }
}
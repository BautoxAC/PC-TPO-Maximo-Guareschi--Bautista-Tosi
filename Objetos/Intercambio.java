package Objetos;

public class Intercambio {


    // objeto utilizado en area premios como intercambio

    private int cantidad; // saldo
    private Premio premioAsignado; // el premio
    private String nombreVisitante;

    public Intercambio(int cantidad, String nombreVisitante) {
        this.cantidad = cantidad;
        this.nombreVisitante = nombreVisitante;
    }

    public int obtenerCantidad() {
        return cantidad;
    }

    public Premio obtenerPremioAsignado() {
        return premioAsignado;
    }

    public String obtenerNombreVisitante() {
        return nombreVisitante;
    }

    public void ponerPremioAsignado(Premio premioAsignado) {
        this.premioAsignado = premioAsignado;
    }
}
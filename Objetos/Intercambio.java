package Objetos;

public class Intercambio {

    private int cantidad;
    private Premio premioAsignado;
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
        return this.nombreVisitante;
    }

    public void ponerPremioAsignado(Premio premioAsignado) {
        this.premioAsignado = premioAsignado;
    }
}
package Objetos;

public class Premio {

    private String nombre;
    private int costo;

    public Premio(String nombre, int costo) {
        this.nombre = nombre;
        this.costo = costo;
    }

    public String obtenerNombre() {
        return nombre;
    }

    public int obtenerCosto() {
        return costo;
    }
}
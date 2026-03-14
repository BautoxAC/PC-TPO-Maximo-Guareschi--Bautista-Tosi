package Objetos;

public class Bolso {

    // bolso de carrera gomones, sirve para guardarlo por el visitante antes de la carrera
    // y es el hilo camioneta quien lo mueve hasta el otro lado
    // luego es obtenido por el mismo visitante que lo dejo

    private String duenio;
    private int cosas;

    public Bolso(String duenio, int cosas) {
        this.duenio = duenio;
        this.cosas = cosas;
    }

    public String obtenerDuenio() {
        return duenio;
    }

    public int obtenerCosas() {
        return cosas;
    }

}
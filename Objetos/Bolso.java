package Objetos;
import Hilos.Visitante;

public class Bolso {

    // bolso de carrera gomones, sirve para guardarlo por el visitante antes de la carrera
    // y es el hilo camioneta quien lo mueve hasta el otro lado
    // luego es obtenido por el mismo visitante que lo dejo

    private int id;
    private Visitante duenio;

    public Bolso(int id, Visitante duenio) {
        this.id = id;
        this.duenio = duenio;
    }

    public int obtenerId() {
        return id;
    }

    public Visitante obtenerDuenio() {
        return duenio;
    }
}
package Objetos;
import Hilos.Visitante;

public class Bolso {

    private final int id;
    private final Visitante duenio;

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
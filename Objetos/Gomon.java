package Objetos;

import Hilos.Visitante;

public class Gomon {

    private boolean esDoble;
    private Visitante conductor;
    private Visitante pasajero;

    public Gomon(Visitante conductor) {
        this.esDoble = false;
        this.conductor = conductor;
    }

    public Gomon(Visitante conductor, Visitante pasajero) {
        this.esDoble = true;
        this.conductor = conductor;
        this.pasajero = pasajero;
    }

    public boolean esDoble() {
        return esDoble;
    }

    public boolean esConductor(Visitante visitante) {
        return conductor == visitante;
    }

    public Visitante obtenerPasajero() {
        return pasajero;
    }

    public Visitante obtenerPareja(Visitante visitante) {

        Visitante retorno = null;

        if (esDoble) {

            if (visitante == conductor) {
                retorno = pasajero;
            } else {
                retorno = conductor;
            }
        }

        return retorno;
    }

}

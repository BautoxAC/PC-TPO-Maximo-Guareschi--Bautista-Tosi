package Objetos;

import java.util.concurrent.Semaphore;

import Hilos.Visitante;

public class Gomon {

    // gomon que se utiliza en carreragomones

    private boolean esDoble; // si es doble o no
    private Visitante conductor; // quien conduce
    private Visitante pasajero; // el pasajero si es doble

    private Semaphore semaforoListo;

    public Gomon(Visitante conductor) { // constructor para gomon individual
        this.esDoble = false;
        this.conductor = conductor;

    }

    public Gomon(Visitante conductor, Visitante pasajero) { // constructor para gomon doble
        this.esDoble = true;
        this.conductor = conductor;
        this.pasajero = pasajero;

        this.semaforoListo = new Semaphore(0);
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

    public Visitante obtenerPareja(Visitante visitante) { // en el caso de que sea doble, la pareja es el pasajero, en el otro caso es el conductor

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

    public void esperarConductor() {
        try {
            semaforoListo.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void avisarPasajero() {
        semaforoListo.release();
    }

}

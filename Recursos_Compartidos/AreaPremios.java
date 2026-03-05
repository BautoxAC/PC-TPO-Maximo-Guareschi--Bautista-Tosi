package Recursos_Compartidos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;

import Hilos.Visitante;
import Objetos.*;

public class AreaPremios {

    private Exchanger<Intercambio> exchanger = new Exchanger<>(); // exchanger para el intercambio entre el encargado del area de premios y el visitante
    private Semaphore capacidad = new Semaphore(1); // semaforo para controlar que solo una persona al mismo tiempo pueda estar
    private Semaphore semaforoVisitante = new Semaphore(0); // semaforo para saber cuando esta listo el intercambio

    private List<Premio> premios = new ArrayList<>(); // lista de premios

    public AreaPremios() {

        premios.add(new Premio("manzana", 9));
        premios.add(new Premio("pelota", 90));
        premios.add(new Premio("pelota de pinpong", 700));
        premios.add(new Premio("raqueta", 200));
        premios.add(new Premio("mando", 300));
        premios.add(new Premio("switch", 500));
        premios.add(new Premio("play", 1000));


    }

    // metodos que usa el visitante

    public Premio canjear() {

        // metodo que hace el visitante para canjear

        Premio premio = null;
        int cantidad;
        Visitante visitante;

        try {

            capacidad.acquire();

            try {

                visitante = (Visitante) Thread.currentThread();

                cantidad = visitante.obtenerSaldo(); // se obtiene el saldo actual del visitante

                Intercambio solicitud = new Intercambio(cantidad,visitante.obtenerNombre()); // crea un objeto solicitud con el nombre del encargado y su saldo
                exchanger.exchange(solicitud); // hace el intercambio con el encargado del area

                semaforoVisitante.acquire(); // espera a que este lista la modificacion de la solicitud

                premio = solicitud.obtenerPremioAsignado(); // obtiene el premio de la solicitud
                visitante.sacarSaldo(premio.obtenerCosto()); // se le remueve el saldo de lo que vale el premio

            } finally {
                capacidad.release();
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return premio;

    }

    public int canjearSaldo() {

        // metodo que le suma el saldo del cliente a la cantidad de fichas que tiene de cada tipo
        // cada ficha tiene cierto valor, es el cual se le multiplica

        int saldoAdquirido = 0;
        Visitante visitante = (Visitante) Thread.currentThread();

        saldoAdquirido += visitante.obtenerFichas("MR") * 3;
        saldoAdquirido += visitante.obtenerFichas("AC") * 4;
        saldoAdquirido += visitante.obtenerFichas("RV") * 6;
        saldoAdquirido += visitante.obtenerFichas("CG") * 7;

        visitante.sacarFichas();

        return saldoAdquirido;

    }

    // metodos que usa el encargado de area de premios

    public void liberarVisitante() {
        semaforoVisitante.release();
    }

    public Intercambio esperarVisitante() {
        Intercambio intercambio = null;
        try {
            intercambio = exchanger.exchange(null); // espera a que un visitante le haga exchange
        } catch (Exception e) {
            System.out.println(e);
        }
        return intercambio;
    }

    public Premio calcularPremio(int cantidad) {

        Premio mejor = null;

        for (Premio p : premios) { // por cada premio en la lista de premios, obtiene el mas caro

            if (cantidad >= p.obtenerCosto()) {

                if (mejor == null || p.obtenerCosto() > mejor.obtenerCosto()) {
                    mejor = p;
                }
            }
        }

        return mejor;
    }

    
}
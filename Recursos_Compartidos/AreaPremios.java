package Recursos_Compartidos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;

import Hilos.Visitante;
import Objetos.*;

public class AreaPremios {

    private Exchanger<Intercambio> exchanger = new Exchanger<>();
    private Semaphore capacidad = new Semaphore(1);
    private Semaphore semaforoVisitante = new Semaphore(0);

    private List<Premio> premios = new ArrayList<>();

    public AreaPremios() {

        premios.add(new Premio("manzana", 13));
        premios.add(new Premio("pelota", 90));
        premios.add(new Premio("pelota de pinpong", 700));
        premios.add(new Premio("raqueta", 200));
        premios.add(new Premio("mando", 300));
        premios.add(new Premio("switch", 500));
        premios.add(new Premio("play", 1000));


    }

    public Premio canjear() {

        Premio premio = null;
        int cantidad;
        Visitante visitante;

        try {

            capacidad.acquire();

            try {

                visitante = (Visitante) Thread.currentThread();

                cantidad = visitante.obtenerSaldo();

                Intercambio solicitud = new Intercambio(cantidad,visitante.obtenerNombre());
                exchanger.exchange(solicitud);

                semaforoVisitante.acquire();

                premio = solicitud.obtenerPremioAsignado();
                visitante.sacarSaldo(premio.obtenerCosto());

            } finally {
                capacidad.release();
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return premio;

    }

    public void liberarVisitante() {
        semaforoVisitante.release();
    }

    public Intercambio esperarVisitante() {
        Intercambio intercambio = null;
        try {
            intercambio = exchanger.exchange(null);
        } catch (Exception e) {
            System.out.println(e);
        }
        return intercambio;
    }

    public Premio calcularPremio(int cantidad) {

        Premio mejor = null;

        for (Premio p : premios) {

            if (cantidad >= p.obtenerCosto()) {

                if (mejor == null || p.obtenerCosto() > mejor.obtenerCosto()) {
                    mejor = p;
                }
            }
        }

        return mejor;
    }

    public int canjearSaldo() {

        int saldoAdquirido = 0;
        Visitante visitante = (Visitante) Thread.currentThread();

        saldoAdquirido += visitante.obtenerFichas("MR") * 3;
        saldoAdquirido += visitante.obtenerFichas("AC") * 4;
        saldoAdquirido += visitante.obtenerFichas("RV") * 6;
        saldoAdquirido += visitante.obtenerFichas("CG") * 7;

        visitante.sacarFichas();

        return saldoAdquirido;

    }

}
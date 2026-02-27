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
        premios.add(new Premio("pelota", 70));
        premios.add(new Premio("pera", 90));
        premios.add(new Premio("play", 1000));


    }

    public Premio canjear(Visitante visitante) {

        Premio premio = null;
        int cantidad;

        try {

            capacidad.acquire();

            try {

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
            // TODO: handle exception
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

    public int canjearSaldo(Visitante visitante) {

        int saldoAdquirido = 0;

        saldoAdquirido += visitante.obtenerFichas("MR") * 3;
        saldoAdquirido += visitante.obtenerFichas("AC") * 4;
        saldoAdquirido += visitante.obtenerFichas("RV") * 6;
        saldoAdquirido += visitante.obtenerFichas("CG") * 7;

        visitante.sacarFichas();

        return saldoAdquirido;

    }

}
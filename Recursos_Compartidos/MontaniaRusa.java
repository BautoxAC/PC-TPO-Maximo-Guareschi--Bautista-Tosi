package Recursos_Compartidos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Objetos.Atraccion;

public class MontaniaRusa implements Atraccion {

    private CyclicBarrier barreraInicio;
    private CyclicBarrier barreraFinal;

    private Semaphore lugares;

    private Semaphore inicioTren;
    private Semaphore mutex;

    private int cantEsperando;

    private boolean actividadAbierta;

    public MontaniaRusa() {

        barreraInicio = new CyclicBarrier(5, () -> {

            inicioTren.release();

        });

        barreraFinal = new CyclicBarrier(6, () -> {

            lugares.release(5);

        });

        lugares = new Semaphore(5);

        inicioTren = new Semaphore(0);
        mutex = new Semaphore(1);

        cantEsperando = 0;

        actividadAbierta = false;
    
    }

    // metodos que hace el visitante

    public boolean entrar() {

        boolean gano = false;

        try {

            mutex.acquire();

            if (cantEsperando < 5 && actividadAbierta) {

                cantEsperando++;

                mutex.release();
                lugares.acquire();
                barreraInicio.await(15, TimeUnit.SECONDS); // espera 15 segundos en la barrera 15 segundos, si no se llena se rompe y se va
                mutex.acquire();

                cantEsperando--;

                gano = true;

            }

            mutex.release();

        } catch (TimeoutException | BrokenBarrierException time) {

            manejarFaltaDeGente();
        
        } catch (Exception e) {
            System.out.println(e);
        }

        return gano;

    }

    public void salir() {

        try {

            barreraFinal.await();
            
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    // metodos que hace el hilo tren montaña rusa

    public void esperarLlenarse() {

        try {
            inicioTren.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void llegar() {

        try {

            barreraFinal.await();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    // metodos privados del recurso compartido

    private void manejarFaltaDeGente() {

        lugares.release();

    }
   
    // metodos de la interfaz atraccion

    public void cerrarActividad() {
        try {
            mutex.acquire();
            actividadAbierta = false;
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean estaAbierta() {
        return actividadAbierta;
    }

    public String obtenerTipoFichas() {
        return "MR";
    }

    public void abrirActividad() {
        try {
            mutex.acquire();
            actividadAbierta = true;
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}

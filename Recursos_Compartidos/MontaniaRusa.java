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
    
    }

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

    public boolean entrar() {

        boolean gano = false;

        try {

            mutex.acquire();

            if (cantEsperando < 5) {

                cantEsperando++;

                mutex.release();
                lugares.acquire();
                barreraInicio.await(30, TimeUnit.SECONDS);
                mutex.acquire();

                cantEsperando--;

                gano = true;

            }

            mutex.release();

        } catch (TimeoutException time) {
            gano = false;
        } catch (BrokenBarrierException time) {
            gano = false;
        
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

    public String obtenerTipoFichas() {
        return "MR";
    }

}

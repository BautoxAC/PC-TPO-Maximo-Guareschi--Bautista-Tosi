package Recursos_Compartidos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import Objetos.Atraccion;

public class RealidadVirtual implements Atraccion {
    private Semaphore[] equipo;
    private int[] capacidades = { 5, 10, 5 };
    private int cantEsperando;
    private Semaphore mutex;
    private Semaphore encargado;
    private Semaphore revisar;
    private AtomicBoolean actividadAbierta;

    public RealidadVirtual() {
        // Dentro de esta BlockingQueue la primera(en posicion 0) es para los visores,
        // la segunda (en posicion 1) es para las manoplas y la tercera (en posicion 2)
        // para las bases
        cantEsperando = 0;
        actividadAbierta = new AtomicBoolean(false);
        equipo = new Semaphore[3];
        mutex = new Semaphore(1);
        for (int i = 0; i < equipo.length; i++) {
            equipo[i] = new Semaphore(capacidades[i]);
        }
        encargado = new Semaphore(0);
        revisar = new Semaphore(0);
    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        try {
            mutex.acquire();

            if (cantEsperando < 4 && estaAbierta()) {

                cantEsperando++;
                mutex.release();

                // Intenta sacar un visor
                if (equipo[0].tryAcquire(2, TimeUnit.SECONDS)) {
                    // Intenta sacar dos manoplas
                    if (equipo[1].tryAcquire(2, 2, TimeUnit.SECONDS)) {
                        // Intenta sacar una base
                        if (equipo[2].tryAcquire(2, TimeUnit.SECONDS)) {
                            // Pudo sacar todo y espera ser revisado
                            entro = true;
                            this.esperarRevisar();

                        } else {

                            // No pudo y devuelve lo que saco
                            equipo[0].release(1);
                            equipo[1].release(2);

                        }
                    } else {
                        equipo[0].release(1);
                    }

                }

                mutex.acquire();
                cantEsperando--;
                mutex.release();

            } else {

                mutex.release();

            }

        } catch (Exception e) {
            System.out.println(e);
            try {
                mutex.acquire();
                cantEsperando--;
                mutex.release();
            } catch (Exception e2) {
                System.out.println(e);
            }

        }
        return entro;
    }

    private void esperarRevisar() {
        try {
            encargado.release();
            revisar.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void salir() {
        try {
            // Pone todo de nuevo
            equipo[0].release(1);
            equipo[1].release(2);
            equipo[2].release(1);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public String obtenerTipoFichas() {
        return "RV";
    }

    public void cerrarActividad() {
        try {
            actividadAbierta.set(false);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean estaAbierta() {
        return actividadAbierta.get();
    }

    //Espera que este listo un visitante

    public void esperarListo() {
        try {
            encargado.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //Deja pasar al visitante
    public void liberaEntrada() {
        try {
            revisar.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    @Override
    public void abrirActividad() {
        try {
            actividadAbierta.set(true);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

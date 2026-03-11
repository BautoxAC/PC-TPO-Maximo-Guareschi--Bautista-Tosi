package Recursos_Compartidos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Objetos.Atraccion;

public class Teatro implements Atraccion {
    private boolean actividadAbierta;
    private boolean estaEncurso;
    private Semaphore mutex;
    private int cantGrupo;
    private Semaphore cupos;
    private CyclicBarrier entrarGrupo;
    private Semaphore salirGrupos;
    private Semaphore teatro;
    private int gruposAdentro;

    public Teatro() {
        mutex = new Semaphore(1);

        actividadAbierta = false;
        estaEncurso = false;
        cantGrupo = 5;
        gruposAdentro = 0;

        // este semaforo es para que despues de cerrado el encargado del teatro no sigo
        // haciendo obras y se vaya a dormir
        teatro = new Semaphore(0);
        cupos = new Semaphore(cantGrupo * 4);
        salirGrupos = new Semaphore(0);

        entrarGrupo = new CyclicBarrier(cantGrupo, () -> {
            try {
                mutex.acquire();
                gruposAdentro++;
                mutex.release();
                System.out.println("Entro el grupo numero " + gruposAdentro);
            } catch (Exception e) {
                System.out.println(e);
            }

        });

    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        try {
            mutex.acquire();
            // Revisa que este abierta la actvidad, no este en curso la obra y haya espacio
            // con los cupos
            if (actividadAbierta && !estaEncurso && cupos.tryAcquire()) {

                mutex.release();
                entrarGrupo.await(15, TimeUnit.SECONDS);
                entro = true;

            } else {
                mutex.release();
            }

        }
       catch (TimeoutException | BrokenBarrierException time) {

            cupos.release();
            entrarGrupo.reset();
            System.out.println(time);

        } catch (Exception e) {
            cupos.release();
            System.out.println(e);
        }

        return entro;
    }

    @Override
    public void salir() {
        try {
            salirGrupos.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void ponerEncurso() {
        try {
            mutex.acquire();
            estaEncurso = true;
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void sacarEnCurso() {
        try {
            // habilita la entrada de personas
            mutex.acquire();
            cupos.release(gruposAdentro * 5);
            gruposAdentro = 0;
            estaEncurso = false;
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void habilitarSalida() {
        try {
            mutex.acquire();
            salirGrupos.release(gruposAdentro * 5);
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void cerrarActividad() {
        try {
            mutex.acquire();
            actividadAbierta = false;
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void abrirActividad() {
        try {
            mutex.acquire();
            teatro.release();
            actividadAbierta = true;
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    @Override
    public boolean estaAbierta() {
        return actividadAbierta;
    }

    @Override
    public String obtenerTipoFichas() {
        return "";
    }

    public void iniciarTeatro() {
        try {
            teatro.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void finalizarTeatro() {
        try {
            mutex.acquire();
            if (actividadAbierta) {
                teatro.release();
            }
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}

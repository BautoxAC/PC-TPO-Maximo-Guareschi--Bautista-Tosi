package Recursos_Compartidos;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import Objetos.Atraccion;

public class Teatro implements Atraccion {
    private boolean actividadAbierta;
    private boolean estaEncurso;
    private Semaphore mutex;
    private int cantGrupo;
    private int gruposAdentro;
    private CyclicBarrier entrarGrupo;
    private Semaphore salirGrupos;

    public Teatro() {
        mutex = new Semaphore(1);
        actividadAbierta = false;
        estaEncurso = false;
        gruposAdentro = 0;
        cantGrupo = 5;
        salirGrupos = new Semaphore(0);
        entrarGrupo = new CyclicBarrier(cantGrupo, () -> {
            try {
                mutex.acquire();
                gruposAdentro++;
                mutex.release();
            } catch (Exception e) {
                // TODO: handle exception
            }
        });

    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        try {
            mutex.acquire();
            if (actividadAbierta && gruposAdentro < 4 && !estaEncurso) {
                mutex.release();
                entrarGrupo.await(15, TimeUnit.SECONDS);
                entro = true;
            } else {
                mutex.release();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return entro;
    }

    @Override
    public void salir() {
        try {
            salirGrupos.acquire();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void ponerEncurso() {
        try {
            mutex.acquire();
            estaEncurso = true;
            mutex.release();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void sacarEnCurso() {
        try {
            mutex.acquire();
            estaEncurso = false;
            salirGrupos.release(gruposAdentro * 5);
            gruposAdentro = 0;
            mutex.release();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void cerrarActividad() {
        try {
            mutex.acquire();
            actividadAbierta = false;
            mutex.release();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void abrirActividad() {
        try {
            mutex.acquire();
            actividadAbierta = true;
            mutex.release();
        } catch (Exception e) {
            // TODO: handle exception
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

}

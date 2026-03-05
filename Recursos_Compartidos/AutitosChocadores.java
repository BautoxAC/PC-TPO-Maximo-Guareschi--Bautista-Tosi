package Recursos_Compartidos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Objetos.Atraccion;

public class AutitosChocadores implements Atraccion {

    private int CAPACIDAD = 20;

    private ReentrantLock lock;
    private Condition hayLugar;
    private Condition encargado;

    private int esperando;
    private boolean enCurso;

    private CyclicBarrier inicio;
    private CyclicBarrier fin;

    private boolean actividadAbierta;

    public AutitosChocadores() {

        lock = new ReentrantLock();
        hayLugar = lock.newCondition();
        encargado = lock.newCondition();

        esperando = 0;
        enCurso = false;
        actividadAbierta = false;

        // en las barreras, el fin es capacidad + 1 por el encargado de los autitos el cual espera una vez inicia la atraccion
        // y la rompe cuando espera el tiempo que tiene en su hilo

        inicio = new CyclicBarrier(CAPACIDAD, () -> {
            System.out.println("Empieza la actividad de los autitos");
        });

        fin = new CyclicBarrier(CAPACIDAD + 1, () -> {
            lock.lock();
            try {
                esperando = 0;
                enCurso = false;
                hayLugar.signalAll();
                System.out.println("Termina la actividad de los autitos");
            } finally {
                lock.unlock();
            }
        });

    }

    // metodos que llaman los visitantes

    public boolean entrar() {

        lock.lock();
        try {

            while (!actividadAbierta || enCurso || esperando == CAPACIDAD) {
                try {
                    hayLugar.await(14, TimeUnit.SECONDS); // si en 14 segundos no se llena, se rompe y retorna que no pudo entrar
                } catch (InterruptedException e) {
                    System.out.println(e);
                    return false;
                }
            }

            esperando++;

            if (esperando == CAPACIDAD) { //si la cantidad de esperando es la capacidad, le avisa al encargado para que se despierte e inicie
                enCurso = true;
                encargado.signal();
            }

        } finally {
            lock.unlock();
        }

        try {
            inicio.await(); // como ya pasaron la condition del lugar, esa cantidad de personas esperan en inicio
            return true;
        } catch (InterruptedException | BrokenBarrierException e) {
            return false;
        }
    }

    // metodos que llaman los dos hilos (visitantes y el encargado autitos)

    public void salir() {
        try {
            fin.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println(e);
        }
    }

    // metodos del encargado de los autitos

    public void esperarLlenarse() {
        lock.lock();
        try {
            while (!enCurso) {
                encargado.await();
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        } finally {
            lock.unlock();
        }

    }

    // metodos de la interfaz atraccion

    public void cerrarActividad() {
        lock.lock();
        try {
            actividadAbierta = false;
            hayLugar.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean estaAbierta() {
        return actividadAbierta;
    }

    public String obtenerTipoFichas() {
        return "AC";
    }

    public void abrirActividad() {
        lock.lock();
        try {
            actividadAbierta = true;
        } finally {
            lock.unlock();
        }
    }

}

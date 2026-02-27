package Recursos_Compartidos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Objetos.Atraccion;

public class AutitosChocadores implements Atraccion {

    private static final int CAPACIDAD = 20;

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
        actividadAbierta = true;

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

    public boolean entrar() {

        lock.lock();
        try {

            while (!actividadAbierta || enCurso || esperando == CAPACIDAD) {
                try {
                    hayLugar.await(14, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    System.out.println(e);
                    return false;
                }
            }

            esperando++;

            if (esperando == CAPACIDAD) {
                enCurso = true;
                encargado.signalAll();
            }

        } finally {
            lock.unlock();
        }

        try {
            inicio.await();
            return true;
        } catch (InterruptedException | BrokenBarrierException e) {
            return false;
        }
    }

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

    public void salir() {
        try {
            fin.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println(e);
        }
    }

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

}

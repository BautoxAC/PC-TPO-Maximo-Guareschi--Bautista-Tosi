package Recursos_Compartidos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

import Objetos.Atraccion;

public class Comedor implements Atraccion {

    private CyclicBarrier inicioComer;
    private final int cantComer = 4;
    private final int cantMesas = 20;
    private ReentrantLock lock;
    private boolean actividadAbierta;
    private int mesasOcupadas;
    private CyclicBarrier fin;

    public Comedor() {
        inicioComer = new CyclicBarrier(cantComer, () -> {
            System.out.println("Empiezan a comer");
        });
        lock = new ReentrantLock();
        mesasOcupadas = 0;
        actividadAbierta = false;
        fin = new CyclicBarrier(cantComer, () -> {
            lock.lock();
            try {
                mesasOcupadas--;
                System.out.println("Terminan de comer");
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        lock.lock();
        if (mesasOcupadas < cantMesas && !actividadAbierta) {
            try {
                inicioComer.await();
                mesasOcupadas++;
                entro = true;
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Mesas llenas");
        }
        lock.unlock();
        return entro;
    }

    @Override
    public void salir() {
        try {
            fin.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println(e);
        }
    }

    @Override
    public void cerrarActividad() {
        lock.lock();
        actividadAbierta = false;
        lock.unlock();
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

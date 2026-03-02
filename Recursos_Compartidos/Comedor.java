package Recursos_Compartidos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import Objetos.Atraccion;

public class Comedor implements Atraccion {

    private CyclicBarrier inicioComer;
    private final int cantComer = 4;
    private final int cantMesas = 20;
    private ReentrantLock lock;
    private boolean actividadAbierta;
    private int limitePersonas;
    private int personasDentro;
    private CyclicBarrier fin;

    public Comedor() {
        inicioComer = new CyclicBarrier(cantComer, () -> {
            System.out.println("Empiezan a comer");
        });
        lock = new ReentrantLock();
        personasDentro = 0;
        limitePersonas = cantComer * cantMesas;
        actividadAbierta = false;
        fin = new CyclicBarrier(cantComer, () -> {
            System.out.println("Terminan de comer");
            lock.lock();
            personasDentro -= 4;
            lock.unlock();
        });
    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        lock.lock();
        if (personasDentro < limitePersonas && actividadAbierta) {
            personasDentro++;
            lock.unlock();
            try {
                inicioComer.await(15, TimeUnit.SECONDS);

                entro = true;
            } catch (Exception e) {
                System.out.println(e);
                lock.lock();
                personasDentro--;
                lock.unlock();
                entro = false;
            }
        } else {
            System.out.println("Mesas llenas");
        }

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

package Recursos_Compartidos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

import Hilos.Visitante;
import Objetos.Atraccion;
import Objetos.Mesa;

public class Comedor implements Atraccion {

    private BlockingQueue<Mesa> mesas;
    private BlockingQueue<Mesa> mesasASalir;
    private CyclicBarrier ponerMesa;
    private final int cantComer = 4;
    private final int cantMesas = 20;
    private ReentrantLock lockPersonas;
    private ReentrantLock lockMesas;
    private boolean actividadAbierta;
    private int limitePersonas;
    private int personasDentro;
    private ConcurrentHashMap<Visitante, Mesa> visitanteAMesa;

    public Comedor() {
        mesas = new ArrayBlockingQueue<>(cantMesas);
        mesasASalir = new ArrayBlockingQueue<>(cantMesas);
        for (int i = 0; i < cantMesas; i++) {
            mesas.offer(new Mesa());
        }

        lockPersonas = new ReentrantLock();
        lockMesas = new ReentrantLock();
        personasDentro = 0;
        limitePersonas = cantComer * cantMesas;
        actividadAbierta = false;
        visitanteAMesa = new ConcurrentHashMap<>();
    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        Mesa mesaActual = mesas.peek();
        lockPersonas.lock();
        Visitante visitante = (Visitante) Thread.currentThread();
        if (personasDentro < limitePersonas && actividadAbierta) {
            personasDentro++;
            lockPersonas.unlock();
            mesaActual = this.rotarHastaSinEstaComiendo3();
            if (!mesaActual.estanComiendo()) {
                entro = mesaActual.entrarMesa();
            }
            if (!entro) {
                lockPersonas.lock();
                personasDentro--;
                lockPersonas.unlock();
            } else {
                visitanteAMesa.put(visitante, mesaActual);
            }
        } else {
            System.out.println("Mesas llenas");
            lockPersonas.unlock();
        }

        return entro;
    }

    @Override
    public void salir() {
        Visitante visitante;
        Mesa mesaActual;
        try {
            visitante = (Visitante) Thread.currentThread();
            mesaActual = visitanteAMesa.remove(visitante);
            mesaActual.salirMesa();
            lockPersonas.lock();
            personasDentro--;
            lockPersonas.unlock();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void cerrarActividad() {
        lockPersonas.lock();
        actividadAbierta = false;
        lockPersonas.unlock();
    }

    @Override
    public boolean estaAbierta() {
        return actividadAbierta;
    }

    @Override
    public String obtenerTipoFichas() {
        return "";
    }

    @Override
    public void abrirActividad() {
        lockPersonas.lock();
        actividadAbierta = true;
        lockPersonas.unlock();
    }

    public Mesa rotarHastaSinEstaComiendo3() {
        Mesa mesaActual = null;
        Mesa mesaRotar;
        int intentos = 0;
        try {
            lockMesas.lock();
            mesaActual = mesas.peek();
            while (mesaActual.estanComiendo() && intentos < 3) {
                mesaRotar = mesas.poll();
                mesas.put(mesaRotar);
                intentos++;
                mesaActual = mesas.peek();
            }
            lockMesas.unlock();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return mesaActual;
    }

}

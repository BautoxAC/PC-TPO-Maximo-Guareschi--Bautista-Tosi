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

    private Mesa[] mesas;
    private final int cantComer = 4;
    private final int cantMesas = 20;
    private ReentrantLock lockPersonas;
    private boolean actividadAbierta;
    private int limitePersonas;
    private int personasDentro;
    private ConcurrentHashMap<Visitante, Mesa> visitanteAMesa;

    public Comedor() {
        mesas = new Mesa[cantMesas];
        for (int i = 0; i < cantMesas; i++) {
            mesas[i] = new Mesa(i);
        }

        lockPersonas = new ReentrantLock();
        personasDentro = 0;
        limitePersonas = cantComer * cantMesas;
        actividadAbierta = false;
        visitanteAMesa = new ConcurrentHashMap<>();
    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        Mesa mesaActual;
        Visitante visitante = (Visitante) Thread.currentThread();
        lockPersonas.lock();
        if (personasDentro < limitePersonas && actividadAbierta) {
            personasDentro++;
            lockPersonas.unlock();
            mesaActual = this.buscarNoComiendoYReservarLugar();
            entro = mesaActual.entrarMesa();
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

    public Mesa buscarNoComiendoYReservarLugar() {
        Mesa mesaActual = mesas[0];
        int i = 0;
        try {
            while (!mesas[i].reservarLugar()) {
                i = (i+1)%mesas.length;
                mesaActual = mesas[i];
            }
        } catch (Exception e) {
           System.out.println(e);
        }
        return mesaActual;
    }

}

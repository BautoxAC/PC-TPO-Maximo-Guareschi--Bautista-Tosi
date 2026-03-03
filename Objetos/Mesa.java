package Objetos;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Mesa {

    private CyclicBarrier inicioComer;
    private CyclicBarrier fin;
    private final int cantComer = 4;
    AtomicBoolean comiendo;

    public Mesa() {
        comiendo = new AtomicBoolean(false);
        inicioComer = new CyclicBarrier(cantComer, () -> {
            System.out.println("Empiezan a comer");
            comiendo.set(true);
        });
        fin = new CyclicBarrier(cantComer, () -> {
            System.out.println("Terminan de comer");
            comiendo.set(false);
        });
    }

    public boolean entrarMesa() {
        boolean entro = false;
        try {
            inicioComer.await(15, TimeUnit.SECONDS);
            
            entro = true;
        } catch (Exception e) {
            entro = false;
        }
        return entro;
    }

    public void salirMesa() {
        try {
            fin.await();
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean estanComiendo() {
        return comiendo.get();
    }
}

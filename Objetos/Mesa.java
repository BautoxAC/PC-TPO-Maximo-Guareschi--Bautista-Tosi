package Objetos;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Mesa {

    private CyclicBarrier inicioComer;
    private CyclicBarrier fin;
    private final int cantComer = 4;
    private int adentro;
    private Semaphore mutex;
    AtomicBoolean comiendo;

    public Mesa(int Num) {
        comiendo = new AtomicBoolean(false);
        inicioComer = new CyclicBarrier(cantComer, () -> {
            System.out.println("Empiezan a comer en mesa " + Num);
            comiendo.set(true);
        });
        mutex = new Semaphore(1);
        adentro = 0;
        fin = new CyclicBarrier(cantComer, () -> {
            System.out.println("Terminan de comer en mesa " + Num);
            comiendo.set(false);
            try {
                mutex.acquire();
                adentro = 0;
                mutex.release();
            } catch (Exception e) {
                // TODO: handle exception
            }

        });
    }

    public boolean entrarMesa() {
        boolean entro = false;
        try {
            mutex.acquire();
            if (!estanComiendo() && adentro < cantComer) {
                adentro++;
                mutex.release();
                try {
                    inicioComer.await(15, TimeUnit.SECONDS);
                    entro = true;
                } catch (Exception e) {
                    entro = false;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
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

package Objetos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Mesa {

    private CyclicBarrier inicioComer;
    private final int cantComer = 4;
    private AtomicInteger adentro;
    private AtomicBoolean comiendo;
    private Semaphore mutex;
    private Semaphore mutexBarrera;
    private int Num;

    public Mesa(int Num) {
        this.Num = Num;
        inicioComer = new CyclicBarrier(cantComer, () -> {
            System.out.println("Empiezan a comer en mesa " + Num);

        });
        mutex = new Semaphore(1);
        adentro = new AtomicInteger(0);
        comiendo = new AtomicBoolean(false);
        mutexBarrera = new Semaphore(1);
    }

    public int num() {
        return this.Num;
    }

    // aca no se controla nada, ya que si o si encuentra una mesa para entrar
    public boolean entrarMesa() {
        boolean entro = false;
        try {
            inicioComer.await(15, TimeUnit.SECONDS);
            entro = true;
        } catch (InterruptedException | BrokenBarrierException e) {
            try {
                mutexBarrera.acquire();
                if (inicioComer.isBroken()) {
                    inicioComer.reset();
                }
                entro = false;
            } catch (Exception r) {
                System.out.println(r);
            } finally {
                mutexBarrera.release();
            }

        } catch (Exception e) {
            entro = false;
        }

        return entro;
    }

    // Este metodo realiza la reserva de lugar de la persona, donde podria haber una
    // condicion de carrera entre que esten comiendo y efectivamente sentarse
    public boolean reservarLugar() {
        boolean reservo = false;
        try {
            mutex.acquire();
            if (!estanComiendo()) {
                adentro.incrementAndGet();
                reservo = true;
                if (adentro.get() == cantComer) {
                    comiendo.set(true);
                }
            }
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
        return reservo;
    }

    public void salirMesa() {
        try {
            mutex.acquire();
            adentro.decrementAndGet();
            if (adentro.get() == 0) {
                comiendo.set(false);
            }
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean estanComiendo() {
        return comiendo.get();
    }
}

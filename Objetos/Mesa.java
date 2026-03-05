package Objetos;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Mesa {

    private CyclicBarrier inicioComer;
    private CyclicBarrier fin;
    private final int cantComer = 4;
    private AtomicInteger adentro;
    private Semaphore mutex;
    private int Num;

    public Mesa(int Num) {
        this.Num = Num;
        inicioComer = new CyclicBarrier(cantComer, () -> {
            System.out.println("Empiezan a comer en mesa " + Num);
        });
        mutex = new Semaphore(1);
        adentro = new AtomicInteger(0);
        fin = new CyclicBarrier(cantComer, () -> {
            System.out.println("Terminan de comer en mesa " + Num);
            adentro.set(0);
        });
    }

    public int num() {
        return this.Num;
    }

    // aca no se controla nada, ya que  si o si encuentra una mesa para entrar
    public boolean entrarMesa() {
        boolean entro = false;
        try {
            try {
                     inicioComer.await(15, TimeUnit.SECONDS);
                     entro = true;
            } catch (Exception e) {
                entro = false;
            }
        } catch (Exception e) {
            System.out.println(e);
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
            }
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
        return reservo;
    }

    public void salirMesa() {
        try {
            fin.await();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean estanComiendo() {
        return adentro.get() == cantComer;
    }
}

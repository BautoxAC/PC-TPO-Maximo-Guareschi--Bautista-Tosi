package Hilos;

import Recursos_Compartidos.MontaniaRusa;

public class TrenMontaniaRusa implements Runnable {

    private MontaniaRusa montaniaRusa;

    public TrenMontaniaRusa(MontaniaRusa montaniaRusa) {

        this.montaniaRusa = montaniaRusa;

    }

    // hilo encargado de esperar a la montaña rusa a que se llene

    public void run() {

        try {

            while (true) {

                System.out.println("Espera a que se llene el tren de la montaña rusa .......");

                montaniaRusa.esperarLlenarse(); // se bloquea esperando a que se llene

                System.out.println("El tren de la montaña rusa inicia el recorrido .....");

                Thread.sleep(6000);

                montaniaRusa.llegar();

            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}

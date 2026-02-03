package Hilos;

import Recursos_Compartidos.MontaniaRusa;

public class TrenMontaniaRusa implements Runnable {

    private MontaniaRusa montaniaRusa;

    public TrenMontaniaRusa(MontaniaRusa montaniaRusa) {

        this.montaniaRusa = montaniaRusa;

    }

    public void run() {

        try {

            while (true) {

                System.out.println("Espera a que se llene el tren de la montaña rusa .......");

                montaniaRusa.esperarLlenarse();

                System.out.println("El tren de la montaña rusa inicia el recorrido .....");

                Thread.sleep(8000);

                montaniaRusa.llegar();

            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}

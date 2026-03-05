package Hilos;

import Recursos_Compartidos.RealidadVirtual;

public class EncargadoRV implements Runnable {

    RealidadVirtual rv;

    public EncargadoRV(RealidadVirtual rv) {
        this.rv = rv;
    }
    // Encarado de la Realidad virtual y se encarga de poner 
    @Override
    public void run() {
        while (true) {

            System.out.println("Esperando revisar persona");

            rv.esperarListo();

            System.out.println("Revisando que este con todo el equipo");

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // TODO: handle exception
            }

            // Deja pasar al visitante
            rv.liberaEntrada();

            System.out.println("Entro visitante a RV");
        }
    }

}

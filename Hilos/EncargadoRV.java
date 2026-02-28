package Hilos;

import Recursos_Compartidos.RealidadVirtual;

public class EncargadoRV implements Runnable {

    RealidadVirtual rv;

    public EncargadoRV(RealidadVirtual rv) {
        this.rv = rv;
    }

    @Override
    public void run() {
        rv.ponerPartes();
        while (true) {
            
            System.out.println("Esperando revisar persona");
            rv.esperarListo();
            System.out.println("Revisar que este con todo el equipo");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // TODO: handle exception
            }
            rv.liberaEntrada();
            System.out.println("Entro visitante a RV");
        }
    }

}

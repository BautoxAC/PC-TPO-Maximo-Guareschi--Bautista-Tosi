package Hilos;

import Objetos.Parque;

public class Propietario extends Thread {

    private Parque parque;

    public Propietario(Parque parque) {
        this.parque = parque;
    }

    public void run() {
        while (true) {
            System.out.println("HORA ACTUAL DEL PARQUE: " + parque.obtenerHora());
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e);
            }

            parque.aumentarHorario();

            if (!parque.estaAbierto()) {

                if (parque.obtenerEstadoActual() == 1) {
                    System.out.println("se abre el parque");
                    parque.abrirActividades();
                    parque.abrirParque();
                } else if (parque.actividadesHabilitadas() && parque.obtenerEstadoActual() == 2) {
                    System.out.println("SE CIERRAN LAS ACTIVIDADES ....");
                    parque.cerrarActividades();
                }

            } else {

                if (parque.obtenerEstadoActual() == 3) {
                    System.out.println("SE CIERRA EL PARQUE");
                    parque.cerrarParque();
                }

            }
           
        }
    }
}

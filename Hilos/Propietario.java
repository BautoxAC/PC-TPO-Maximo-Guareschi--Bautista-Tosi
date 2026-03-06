package Hilos;

import Recursos_Compartidos.Parque;

public class Propietario extends Thread {

    private Parque parque;

    public Propietario(Parque parque) {
        this.parque = parque;
    }

    // hilo encargado de manejar la hora del parque

    // en base al estado actual del parque que se termina por el horario hace una cosa u otra

    public void run() {
        while (true) {
            parque.aumentarHorario();
            System.out.println("HORA ACTUAL DEL PARQUE: " + parque.obtenerHora());


            if (!parque.estaAbierto()) {

                if (parque.obtenerEstadoActual() == 1) { // abre parque
                    System.out.println("se abre el parque");
                    parque.abrirActividades();
                    parque.abrirParque();
                } else if (parque.actividadesHabilitadas() && parque.obtenerEstadoActual() == 2) { // cierra actividades
                    System.out.println("SE CIERRAN LAS ACTIVIDADES ......................");
                    parque.cerrarActividades();
                }

            } else {

                if (parque.obtenerEstadoActual() == 3) { // cierra parque
                    System.out.println("SE CIERRA EL PARQUE");
                    parque.cerrarParque();
                }

            }
            try {
                Thread.sleep(6000);
            } catch (Exception e) {
                System.out.println(e);
            }
           
        }
    }
}

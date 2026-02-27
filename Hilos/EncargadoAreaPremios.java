package Hilos;

import Recursos_Compartidos.AreaPremios;
import Objetos.*;

public class EncargadoAreaPremios implements Runnable {

    private AreaPremios area;

    public EncargadoAreaPremios(AreaPremios area) {
        this.area = area;
    }

    public void run() {

        while (true) {

                System.out.println("el encargado del area de premios espera a un visitante... ");

                Intercambio intercambio = area.esperarVisitante();

                System.out.println("encargado recibe " + intercambio.obtenerCantidad());

                Premio premio = area.calcularPremio(intercambio.obtenerCantidad());

                intercambio.ponerPremioAsignado(premio);

                if (premio != null) {
                    System.out.println("Entrega premio: " + premio.obtenerNombre() + " al visitante "+intercambio.obtenerNombreVisitante());
                } else {
                    System.out.println("No alcanza para premio.");
                }

                area.liberarVisitante();

            }
    }
}
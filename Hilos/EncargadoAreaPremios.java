package Hilos;

import Recursos_Compartidos.AreaPremios;
import Objetos.*;

public class EncargadoAreaPremios implements Runnable {

    // encargado de intercambiar con un visitante cuando llega al area de premios

    private AreaPremios area;

    public EncargadoAreaPremios(AreaPremios area) {
        this.area = area;
    }

    public void run() {

        while (true) {

                System.out.println("el encargado del area de premios espera a un visitante... ");

                Intercambio intercambio = area.esperarVisitante(); // se bloquea esperando otro exchange

                System.out.println("encargado recibe "+ intercambio.obtenerCantidad()); // muestra el saldo del intercambio

                Premio premio = area.calcularPremio(intercambio.obtenerCantidad()); // selecciona el mayor premio con el saldo obtenido

                intercambio.ponerPremioAsignado(premio); // pone el premio

                if (premio != null) {
                    System.out.println("Entrega premio: " + premio.obtenerNombre() + " al visitante "+intercambio.obtenerNombreVisitante());
                } else {
                    System.out.println("No alcanza para premio.");
                }

                area.liberarVisitante(); // libera el semaforo para avisarle al visitante que ya esta el premio

            }
    }
}
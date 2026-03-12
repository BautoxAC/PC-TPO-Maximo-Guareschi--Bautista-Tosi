package Test;

import Hilos.Propietario;
import Hilos.Visitante;
import Hilos.WriterVisitante;
import Recursos_Compartidos.Parque;

public class Main {
    public static void main (String[] args) {
        System.out.println("Iniciando simulacion del Parque");
        
        Parque parque = new Parque(100);
        Propietario propietario = new Propietario(parque);
        propietario.start();
        WriterVisitante wri = new WriterVisitante();
        for (int i = 1; i <= 100; i++) {
            Visitante v = new Visitante("V-" + i, parque, wri);
            v.start();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }
}
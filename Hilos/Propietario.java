package Hilos;

import Objetos.Parque;

public class Propietario {

    private Parque parque;

    // CONSTRUCTOR
    public Propietario(Parque parque){
        this.parque = parque;
    }

    public void run(){
        while(true){
            System.out.println("----------------- SON LAS: "  + parque.obtenerHorario() + ":00 -----------------" );
            try {
                Thread.sleep(20000);
            } catch (Exception e) {
            }
            parque.aumentarHorario();
            parque.esHorarioCierre();
            parque.esHorarioCierreActividades();
            if (parqueX.esHorarioApertura()) {
                System.out.println("EL PARQUE ABRIO SUS PUERTAS!!!");
                parque.abrirActividades();
                parque.abrirParque();
            }
        }
    }
}



package Objetos;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


import Hilos.*;
import Recursos_Compartidos.*;

public class Parque {

    private Semaphore molinetes;
    private Semaphore mutexParque;

    public Atraccion[] atracciones;
    public AreaPremios areaPremios;

    private boolean parqueAbierto;
    private int hora;

    public Parque(int cantMolinetes) {

        molinetes = new Semaphore(cantMolinetes);
        mutexParque = new Semaphore(1);

        this.parqueAbierto = true;
        this.hora = 0;

        atracciones = new Atraccion[6];
        atracciones[0] = new MontaniaRusa();
        atracciones[1] = new AutitosChocadores();
        atracciones[2] = new RealidadVirtual();
        atracciones[3] = new CarreraGomones();
        
        areaPremios = new AreaPremios();
        

        TrenMontaniaRusa trenMontaniaRusa = new TrenMontaniaRusa((MontaniaRusa) atracciones[0]);
        new Thread(trenMontaniaRusa).start();

        EncargadoAutitos encargadoAutos = new EncargadoAutitos((AutitosChocadores) atracciones[1]);
        new Thread(encargadoAutos).start();

        EncargadoRV encargadoRV = new EncargadoRV((RealidadVirtual) atracciones[2]);
        new Thread(encargadoRV).start();

        Camioneta camioneta = new Camioneta((CarreraGomones) atracciones[3]);
        new Thread(camioneta).start();

        EncargadoAreaPremios encargadoPremios = new EncargadoAreaPremios(areaPremios);
        new Thread(encargadoPremios).start();


    }

    public boolean intentarEntrar() {

        boolean exito = false;

        try {

            molinetes.acquire();

            mutexParque.acquire();

            exito = this.parqueAbierto;

            mutexParque.release();

        } catch (Exception e) {
            System.out.println(e);
        }

        return exito;

    }

    public void liberarMolinete() {

        molinetes.release();

    }

    public void abrirParque() {

        try {
            mutexParque.acquire();

            this.parqueAbierto = true;
            abrirActividades();

            System.out.println("=== PARQUE ABIERTO (09:00) ===");

            mutexParque.release();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void cerrarParque() {

        try {
            mutexParque.acquire();

            this.parqueAbierto = false;
            cerrarActividades();

            System.out.println("=== PARQUE CERRADO (18:00) ===");

            mutexParque.release();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void abrirActividades() {

        System.out.println("=== ACTIVIDADES ABRIENDO ===");
        
        for (int i = 0; i < atracciones.length; i++) {
            if (atracciones[i] != null) {
                //atracciones[i].abrirActividad();
            } 
        }

    }

    public void cerrarActividades() {
        System.out.println("=== ACTIVIDADES CERRANDO (19:00) ===");
        
        for (int i = 0; i < atracciones.length; i++) {
            if (atracciones[i] != null) {
                atracciones[i].cerrarActividad();
            } 
        }

    }

    

    // public int getPersonasEnMontañaRusa() {
    // return montañaRusa.getPersonasEsperando();
    // }

    public Atraccion obtenerAtraccion(String actividad) {

        return atracciones[this.traducirActividad(actividad)];

    }

    // Metodo que recibe le actividad y devuelve el numero correspondiente para las fichas y las actividades

    public int traducirActividad(String actividad) {

        int num;

        switch (actividad) {
            case "MR":
                num = 0;
                break;
            case "AC":
                num = 1;
                break;
            case "RV":
                num = 2;
                break;
            case "CG":
                num = 3;
                break;
            default:
                num = 0;
                break;
        }

        return num;

    }

    public int obtenerValoresFicha(String actividad) {

        int num;

        switch (actividad) {
            case "MR":
                num = 3;
                break;
            case "AC":
                num = 4;
                break;
            case "RV":
                num = 3;
                break;
            case "CG":
                num = 4;
                break;
            default:
                num = 1;
                break;
        }

        return num;

    }

    public boolean estaAbierto() {
        return this.parqueAbierto;
    }

    public int canjearSaldo(Visitante visitante) {
        return areaPremios.canjearSaldo(visitante);
    }

    public Premio entrarAreaPremios(Visitante visitante) {
        return areaPremios.canjear(visitante);
    }

}

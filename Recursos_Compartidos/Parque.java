package Recursos_Compartidos;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import Hilos.*;
import Objetos.Atraccion;
import Objetos.Premio;

public class Parque {

    private Semaphore molinetes; // molinetes para el paso de los visitantes al parque

    public Atraccion[] atracciones; // arreglo de las atracciones
    public AreaPremios areaPremios; // area de premios (no es atraccion)

    private AtomicBoolean parqueAbierto; // booleano atomico para determinar si el parque esta abierto o no
    private boolean actividadesAbiertas; // booleano para saber si las actividades estan abiertas o no
    private int hora;

    public Parque(int cantMolinetes) {

        molinetes = new Semaphore(cantMolinetes);

        this.parqueAbierto = new AtomicBoolean(false);
        this.actividadesAbiertas = false;

        this.hora = 8;

        // se inicializan los objetos y los hilos correspondientes, como son del parque y estan dentro de el
        // se hace en parque y no en otro lugar

        atracciones = new Atraccion[6];
        atracciones[0] = new MontaniaRusa();
        atracciones[1] = new AutitosChocadores();
        atracciones[2] = new RealidadVirtual();
        atracciones[3] = new CarreraGomones();
        atracciones[4] = new Comedor();
        atracciones[5] = new Teatro();

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

        EncargadoTeatro encargadoTeatro  = new EncargadoTeatro((Teatro)atracciones[5]);
        new Thread(encargadoTeatro).start();

    }

    // metodos que hace el visitante a la hora de entrar al parque

    public boolean intentarEntrar() {

        // metodo que hace el visitante para tratar de entrar al parque

        boolean exito = false;

        try {

            molinetes.acquire();

            exito = this.parqueAbierto.get();

        } catch (Exception e) {
            System.out.println(e);
        }

        return exito;

    }

    public void liberarMolinete() {

        molinetes.release();

    }

    // metodos de manejo del parque (abrir/cerrar parque, abrir/cerrar actividades y aumentar horario por parte del propietario)

    public void abrirParque() {

        try {
 
            this.parqueAbierto.set(true);
            abrirActividades();

            System.out.println("=== PARQUE ABIERTO (09:00) ===");

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void cerrarParque() {

        try {

            this.parqueAbierto.set(false);
            cerrarActividades();

            System.out.println("=== PARQUE CERRADO (18:00) ===");

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void abrirActividades() {

        System.out.println("=== ACTIVIDADES ABRIENDO ===");

        actividadesAbiertas = true;

        for (int i = 0; i < atracciones.length; i++) {
            if (atracciones[i] != null) {
                atracciones[i].abrirActividad();
            }
        }

    }

    public void cerrarActividades() {
        System.out.println("=== ACTIVIDADES CERRANDO (19:00) ===");

        actividadesAbiertas = false;

        for (int i = 0; i < atracciones.length; i++) {
            if (atracciones[i] != null) {
                atracciones[i].cerrarActividad();
            }
        }

    }

    public void aumentarHorario() {

        hora = (hora + 1) % 24;

    }

    // metodos de areapremios invocados por el visitante

    public Premio entrarAreaPremios() {
        return areaPremios.canjear();
    }

    public int canjearSaldo() {

        // metodo que hace el visitante para canjear las fichas por saldo

        return areaPremios.canjearSaldo();
    }
    
    // metodos traductores y de obtencion de valores

    public boolean estaAbierto() {
        return this.parqueAbierto.get();
    }

    public boolean actividadesHabilitadas() {

        // metodo que devuelve si las actividades estan abiertas o no

        return actividadesAbiertas;
    }

    public Atraccion obtenerAtraccion(String actividad) {

        // metodo para obtener la atraccion del arreglo en base al nombre

        return atracciones[this.traducirActividad(actividad)];

    }

    public int obtenerHora() {
        return hora;
    }

    public int obtenerEstadoActual() {

        // metodo que, en base a la hora, se devuelve un estado para determinar que hace el propietario del parque (hilo propietario)

        int estado = 0;

        if (hora >= 9 && hora <= 17) {
            estado = 1; // se abre el parque
        } else if (hora == 18) {
            estado = 3; // se cierra el parque
        } else if (hora == 19) {
            estado = 2; // se cierran las actividades
        }
        
        return estado;

    }

    public int traducirActividad(String actividad) {

        // Metodo que recibe le actividad y devuelve el numero correspondiente para las
        // fichas y las actividades

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
            case "CO":
                num = 4;
                break;
            case "TE":
                num = 5;
                break;
            default:
                num = 0;
                break;
        }

        return num;

    }

    public int obtenerValoresFicha(String actividad) {

        // metodo para saber cuanto se da de cada ficha al terminar una actividad

        int num;

        switch (actividad) {
            case "MR":
                num = 20;
                break;
            case "AC":
                num = 45;
                break;
            case "RV":
                num = 35;
                break;
            case "CG":
                num = 30;
                break;
            default:
                num = 0;
                break;
        }

        return num;

    }


}

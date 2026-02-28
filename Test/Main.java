package Test;

import Hilos.EncargadoRV;
import Hilos.Visitante;
import Objetos.Parque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Iniciando simulacion del Parque");
        
        Parque parque = new Parque(4);
        
        parque.abrirParque();
  
        for (int i = 1; i <= 6; i++) {
            Visitante v = new Visitante("V-" + i, parque);
            new Thread(v).start();
            
            Thread.sleep(10 + (int)(Math.random() * 10));
            
            
        }

        Thread.sleep(61000);

        Thread.sleep(100 + (int)(Math.random() * 700));
            
        parque.cerrarActividades();
        
        Thread.sleep(2000);

        parque.cerrarParque();
        
        System.out.println("Parque cerrado");
    }
}
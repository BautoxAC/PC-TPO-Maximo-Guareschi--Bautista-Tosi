package Objetos;

public interface Atraccion {


    // elegimos hacer una interfaz comun para cada una de las atracciones, respetando sus metodos y para hacer que el visitante tenga
    // una atraccion al mismo tiempo como variable

    public boolean entrar();

    public void salir();

    public void cerrarActividad();

    public void abrirActividad();

    public boolean estaAbierta();

    public String obtenerTipoFichas();

}

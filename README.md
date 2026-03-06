README PARA LA EJECUCION DEL PROGRAMA DE MAXIMO GUARESCHI y JUAN BAUTISTA TOSI GRIEDASSOV
SE SUPONE PARA ESTO QUE NO EXISTE UN Main, PERO YA HAY UNO EN EL ARCHIVO EN CASO DE QUE SE QUIERA UTILIZAR

1) Crear un Main.java
2) Crear un objeto Parque en el main del Main, como parámetro se mandan la cantidad de molinetes de acceso al parque.
3) Se crea un objeto Propietario con un único parámetro que es el parque.
4) Se inicia el hilo propietario, como extiende a Thread, con el método start()
5) Se inicializan hilos visitantes, son del tipo Visitante y los parámetros son nombre y el parque. (Preferiblemente deben tener nombres distintos por que hay actividades que utilizan sus nombres como ID)
6) Se inicializan los hilos visitantes con start() ya que extienden a Thread.
7) Ya con eso habría un parque ejecutándose con visitantes, el resto de hilos necesarios para el funcionamiento del parque se inicializan dentro del parque, tanto ellos como los recursos compartidos que utilizaran.
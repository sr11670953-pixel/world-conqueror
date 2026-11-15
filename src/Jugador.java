public class Jugador {
    private final String nombre;
    private int puntos = 0;

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int p) { this.puntos = p; }
}

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private JFrame frame;
    private List<Jugador> jugadores;
    private PanelJuego panelJuego;

    public Game() {
        jugadores = new ArrayList<>();
        // Crear 4 jugadores de ejemplo (puedes cambiar nombres)
        jugadores.add(new Jugador("Jugador 1"));
        jugadores.add(new Jugador("Jugador 2"));
        jugadores.add(new Jugador("Jugador 3"));
        jugadores.add(new Jugador("Jugador 4"));
    }

    public void start() {
        frame = new JFrame("World's Greatest Conqueror - Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panelJuego = new PanelJuego(jugadores);
        frame.setContentPane(panelJuego);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

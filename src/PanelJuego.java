import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Random;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * PanelJuego: contiene el mapa 10x10 (GridLayout), panel de info y controles.
 * Estética moderna sin cambiar la mecánica básica.
 */
public class PanelJuego extends JPanel {
    private final int FILAS = 10;
    private final int COLS = 10;
    private Territorio[] mapa;
    private List<Jugador> jugadores;
    private Color[] coloresJugadores;
    private int jugadorActualIndex = 0;
    private int faseActual = 0; // 0 refuerzos,1 ataque,2 movilizacion
    private final String[] FASES = {"Refuerzos", "Ataque", "Movilización"};

    // Componentes UI
    private JLabel lblFase;
    private JLabel lblJugador;
    private JTextArea logArea;
    private JButton btnConfirmar;
    private JPanel mapaPanel;

    private Random rnd = new Random();

    public PanelJuego(List<Jugador> jugadores) {
        this.jugadores = jugadores;
        this.coloresJugadores = new Color[jugadores.size()];

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 700));
        initUI();
        inicializarMapa();
        asignarTerritoriosIniciales();
        log("Juego iniciado. Comienza " + jugadores.get(0).getNombre() + " en fase Refuerzos.");
        actualizarInfo();
    }

    private void initUI() {
        // --- MAPA (center) ---
        mapaPanel = new JPanel(new GridLayout(FILAS, COLS, 4, 4)); // gaps para separacion
        mapaPanel.setBackground(new Color(40, 40, 40));
        mapaPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(mapaPanel, BorderLayout.CENTER);

        // --- PANEL INFO (east) ---
        JPanel info = new JPanel();
        info.setLayout(new BorderLayout());
        info.setPreferredSize(new Dimension(320, 0));
        info.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JPanel topInfo = new JPanel();
        topInfo.setLayout(new BoxLayout(topInfo, BoxLayout.Y_AXIS));
        topInfo.setBorder(BorderFactory.createTitledBorder("Información de Turno"));
        lblFase = new JLabel();
        lblFase.setFont(lblFase.getFont().deriveFont(Font.BOLD, 14f));
        lblJugador = new JLabel();
        topInfo.add(lblFase);
        topInfo.add(Box.createRigidArea(new Dimension(0,6)));
        topInfo.add(lblJugador);
        topInfo.add(Box.createRigidArea(new Dimension(0,6)));
        topInfo.add(new JLabel("Tropas disponibles: X"));

        info.add(topInfo, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createTitledBorder("Registro de Eventos"));
        info.add(sp, BorderLayout.CENTER);

        add(info, BorderLayout.EAST);

        // --- CONTROLES (south) ---
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnConfirmar = new JButton("Confirmar Refuerzos y Pasar a Ataque");
        btnConfirmar.addActionListener((ActionEvent e) -> avanzarFase());
        controls.add(btnConfirmar);

        add(controls, BorderLayout.SOUTH);
    }

    private void inicializarMapa() {
        int total = FILAS * COLS;
        mapa = new Territorio[total];
        for (int i = 0; i < total; i++) {
            Territorio t = new Territorio("T" + (i + 1));
            mapa[i] = t;

            // estilo adicional por si quieres algo distinto
            t.setBackground(new Color(210,210,210));
            t.setBorder(BorderFactory.createLineBorder(new Color(90,90,90), 1));
            t.setMargin(new Insets(6,6,6,6));
            t.setFocusPainted(false);

            // acción
            final Territorio ref = t;
            t.addActionListener(ev -> manejarClickTerritorio(ref));

            mapaPanel.add(t);
        }
    }

    private void asignarTerritoriosIniciales() {
        // Asignación simple: repartir aleatoriamente 1 territorio por jugador al inicio
        int total = mapa.length;
        boolean[] ocupado = new boolean[total];

        for (int i = 0; i < jugadores.size(); i++) {
            // generar color para el jugador (pastel)
            Color c = generarColorPastel();
            coloresJugadores[i] = c;

            // buscar índice libre aleatorio
            int idx;
            do {
                idx = rnd.nextInt(total);
            } while (ocupado[idx]);

            ocupado[idx] = true;
            mapa[idx].setPropietario(jugadores.get(i));
            mapa[idx].setBackground(c);
            mapa[idx].agregarTropas(1);
        }
    }

    private Color generarColorPastel() {
        int r = 120 + rnd.nextInt(120);
        int g = 120 + rnd.nextInt(120);
        int b = 120 + rnd.nextInt(120);
        return new Color(r, g, b);
    }

    private void manejarClickTerritorio(Territorio t) {
        Jugador actual = jugadores.get(jugadorActualIndex);

        if (faseActual == 0) { // Refuerzos
            if (t.getPropietario() == actual) {
                t.agregarTropas(1);
                log(actual.getNombre() + " coloca 1 tropa en " + t.getNombre() + " (Total: " + t.getTropas() + ")");
            } else {
                JOptionPane.showMessageDialog(this, "Solo puedes colocar refuerzos en tus territorios.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        } else if (faseActual == 1) { // Ataque
            log("Seleccionado " + t.getNombre() + " en fase ATAQUE. (Implementa tu flujo de ataque aquí)");
            // aquí puedes implementar selección origen/destino
        } else if (faseActual == 2) { // Movilización
            log("Seleccionado " + t.getNombre() + " en fase MOVILIZACIÓN. (Implementa tu flujo de movimiento aquí)");
        }
    }

    private void avanzarFase() {
        Jugador actual = jugadores.get(jugadorActualIndex);
        if (faseActual == 0) {
            log(actual.getNombre() + " ha terminado la colocación de refuerzos.");
            faseActual = 1;
            btnConfirmar.setText("Finalizar Ataques y Pasar a Movilización");
        } else if (faseActual == 1) {
            log(actual.getNombre() + " ha finalizado sus ataques.");
            faseActual = 2;
            btnConfirmar.setText("Finalizar Movilización y Terminar Turno");
        } else {
            log(actual.getNombre() + " ha finalizado la movilización. Fin de turno.");
            faseActual = 0;
            cambiarJugador();
            btnConfirmar.setText("Confirmar Refuerzos y Pasar a Ataque");
        }
        actualizarInfo();
    }

    private void cambiarJugador() {
        jugadorActualIndex = (jugadorActualIndex + 1) % jugadores.size();
        log("--- ¡Cambio de Turno! Comienza el turno de " + jugadores.get(jugadorActualIndex).getNombre() + " ---");
    }

    private void actualizarInfo() {
        lblFase.setText("Fase Actual: " + FASES[faseActual]);
        Jugador j = jugadores.get(jugadorActualIndex);
        lblJugador.setText("Jugador Actual: " + j.getNombre() + " | Color: " + colorToStr(coloresJugadores[jugadorActualIndex]));
    }

    private String colorToStr(Color c) {
        if (c == null) return "N/A";
        return "RGB(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")";
    }

    private void log(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}


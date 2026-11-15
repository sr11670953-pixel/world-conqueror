import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * PanelJuego: mapa de 10x10 territorios, panel de información y controles.
 *
 * Reglas (inspiradas en Risk):
 * - Al inicio los 100 territorios se reparten por igual entre los jugadores, con 1 tropa cada uno.
 * - Refuerzos: al empezar el turno recibes max(3, territorios / 3) tropas y debes colocarlas todas.
 * - Ataque: eliges un territorio tuyo con 2 o más tropas y luego un vecino enemigo.
 *   Cada clic es una batalla con dados (atacante hasta 3, defensor hasta 2).
 *   Si el defensor se queda sin tropas, conquistas el territorio.
 * - Movilización: eliges un territorio tuyo y luego un vecino tuyo; cada clic mueve 1 tropa.
 *   Solo puedes hacer un movimiento (un par origen-destino) por turno.
 * - Gana quien conquiste todo el mapa. Los jugadores sin territorios quedan eliminados.
 */
public class PanelJuego extends JPanel {
    private static final int FILAS = 10;
    private static final int COLS = 10;
    private static final String[] FASES = {"Refuerzos", "Ataque", "Movilización"};
    private static final Color[] COLORES = {
        new Color(231, 111, 81),   // naranja
        new Color(42, 157, 143),   // verde azulado
        new Color(233, 196, 106),  // amarillo
        new Color(130, 120, 220)   // morado
    };

    private Territorio[] mapa;
    private final List<Jugador> jugadores;
    private final Color[] coloresJugadores;
    private int jugadorActualIndex = 0;
    private int faseActual = 0; // 0 refuerzos, 1 ataque, 2 movilización
    private int tropasDisponibles = 0;
    private boolean juegoTerminado = false;

    // Selección para ataque y movilización
    private Territorio origen = null;
    private Territorio origenMovido = null;   // par usado en la movilización de este turno
    private Territorio destinoMovido = null;

    // Componentes UI
    private JLabel lblFase;
    private JLabel lblJugador;
    private JLabel lblTropas;
    private JLabel lblAyuda;
    private JTextArea logArea;
    private JButton btnConfirmar;
    private JPanel mapaPanel;

    private final Random rnd;

    public PanelJuego(List<Jugador> jugadores) {
        this(jugadores, new Random());
    }

    // Permite pasar un Random con semilla para probar el juego de forma repetible
    public PanelJuego(List<Jugador> jugadores, Random rnd) {
        this.jugadores = jugadores;
        this.rnd = rnd;
        this.coloresJugadores = new Color[jugadores.size()];
        for (int i = 0; i < jugadores.size(); i++) {
            coloresJugadores[i] = COLORES[i % COLORES.length];
        }

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 700));
        initUI();
        inicializarMapa();
        asignarTerritoriosIniciales();
        log("Juego iniciado. Comienza " + jugadorActual().getNombre() + ".");
        iniciarTurno();
    }

    // ---------------------------------------------------------------- UI

    private void initUI() {
        mapaPanel = new JPanel(new GridLayout(FILAS, COLS, 4, 4));
        mapaPanel.setBackground(new Color(40, 40, 40));
        mapaPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(mapaPanel, BorderLayout.CENTER);

        JPanel info = new JPanel(new BorderLayout());
        info.setPreferredSize(new Dimension(340, 0));
        info.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topInfo = new JPanel();
        topInfo.setLayout(new BoxLayout(topInfo, BoxLayout.Y_AXIS));
        topInfo.setBorder(BorderFactory.createTitledBorder("Información de Turno"));
        lblFase = new JLabel();
        lblFase.setFont(lblFase.getFont().deriveFont(Font.BOLD, 14f));
        lblJugador = new JLabel();
        lblTropas = new JLabel();
        lblAyuda = new JLabel();
        topInfo.add(lblFase);
        topInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        topInfo.add(lblJugador);
        topInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        topInfo.add(lblTropas);
        topInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        topInfo.add(lblAyuda);
        info.add(topInfo, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createTitledBorder("Registro de Eventos"));
        info.add(sp, BorderLayout.CENTER);

        add(info, BorderLayout.EAST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnConfirmar = new JButton();
        btnConfirmar.addActionListener((ActionEvent e) -> avanzarFase());
        controls.add(btnConfirmar);
        add(controls, BorderLayout.SOUTH);
    }

    private void inicializarMapa() {
        mapa = new Territorio[FILAS * COLS];
        for (int i = 0; i < mapa.length; i++) {
            Territorio t = new Territorio("T" + (i + 1), i);
            t.setMargin(new Insets(6, 6, 6, 6));
            final Territorio ref = t;
            t.addActionListener(ev -> manejarClickTerritorio(ref));
            mapa[i] = t;
            mapaPanel.add(t);
        }
    }

    // Reparte todo el mapa por igual entre los jugadores, en orden aleatorio
    private void asignarTerritoriosIniciales() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < mapa.length; i++) indices.add(i);
        Collections.shuffle(indices, rnd);

        for (int k = 0; k < indices.size(); k++) {
            Territorio t = mapa[indices.get(k)];
            cambiarPropietario(t, jugadores.get(k % jugadores.size()));
            t.agregarTropas(1);
        }
    }

    // ---------------------------------------------------------------- Turnos

    private void iniciarTurno() {
        faseActual = 0;
        origen = null;
        origenMovido = null;
        destinoMovido = null;
        tropasDisponibles = Math.max(3, contarTerritorios(jugadorActual()) / 3);
        log(jugadorActual().getNombre() + " recibe " + tropasDisponibles + " tropas de refuerzo.");
        actualizarInfo();
    }

    private void avanzarFase() {
        if (juegoTerminado) return;
        Jugador actual = jugadorActual();

        if (faseActual == 0) {
            if (tropasDisponibles > 0) {
                log("Aún te quedan " + tropasDisponibles + " tropas por colocar.");
                return;
            }
            log(actual.getNombre() + " termina sus refuerzos y pasa al ataque.");
            faseActual = 1;
        } else if (faseActual == 1) {
            log(actual.getNombre() + " termina sus ataques.");
            faseActual = 2;
        } else {
            log(actual.getNombre() + " termina su turno.");
            cambiarJugador();
            iniciarTurno();
            return;
        }
        seleccionar(null);
        actualizarInfo();
    }

    // Pasa al siguiente jugador que todavía tenga territorios
    private void cambiarJugador() {
        do {
            jugadorActualIndex = (jugadorActualIndex + 1) % jugadores.size();
        } while (contarTerritorios(jugadorActual()) == 0);
        log("--- Turno de " + jugadorActual().getNombre() + " ---");
    }

    // ---------------------------------------------------------------- Clics

    private void manejarClickTerritorio(Territorio t) {
        if (juegoTerminado) return;
        switch (faseActual) {
            case 0: colocarRefuerzo(t); break;
            case 1: clicAtaque(t); break;
            default: clicMovilizacion(t); break;
        }
        actualizarInfo();
    }

    private void colocarRefuerzo(Territorio t) {
        if (t.getPropietario() != jugadorActual()) {
            log("Solo puedes reforzar tus propios territorios.");
            return;
        }
        if (tropasDisponibles == 0) {
            log("Ya colocaste todos tus refuerzos. Pulsa el botón para atacar.");
            return;
        }
        t.agregarTropas(1);
        tropasDisponibles--;
        log("+1 tropa en " + t.getNombre() + " (total " + t.getTropas() + ").");
    }

    private void clicAtaque(Territorio t) {
        Jugador actual = jugadorActual();

        if (t.getPropietario() == actual) {
            if (t.getTropas() < 2) {
                log(t.getNombre() + " necesita al menos 2 tropas para atacar.");
                return;
            }
            seleccionar(t == origen ? null : t);
            if (origen != null) log("Atacando desde " + t.getNombre() + ". Elige un vecino enemigo.");
            return;
        }

        if (origen == null) {
            log("Primero elige uno de tus territorios con 2 o más tropas.");
            return;
        }
        if (!sonVecinos(origen, t)) {
            log(t.getNombre() + " no es vecino de " + origen.getNombre() + ".");
            return;
        }
        batalla(origen, t);
    }

    private void clicMovilizacion(Territorio t) {
        Jugador actual = jugadorActual();
        if (t.getPropietario() != actual) {
            log("En movilización solo puedes usar tus territorios.");
            return;
        }

        if (origen == null || t == origen) {
            if (origenMovido != null && t != origenMovido) {
                log("Este turno ya moviste tropas desde " + origenMovido.getNombre() + ".");
                return;
            }
            if (t.getTropas() < 2) {
                log(t.getNombre() + " necesita al menos 2 tropas para mover alguna.");
                return;
            }
            seleccionar(t == origen ? null : t);
            if (origen != null) log("Moviendo desde " + t.getNombre() + ". Haz clic en un vecino tuyo para mover 1 tropa.");
            return;
        }

        if (!sonVecinos(origen, t)) {
            log(t.getNombre() + " no es vecino de " + origen.getNombre() + ".");
            return;
        }
        if (destinoMovido != null && t != destinoMovido) {
            log("Este turno solo puedes mover tropas hacia " + destinoMovido.getNombre() + ".");
            return;
        }
        if (origen.getTropas() < 2) {
            log(origen.getNombre() + " debe conservar al menos 1 tropa.");
            return;
        }
        origen.agregarTropas(-1);
        t.agregarTropas(1);
        origenMovido = origen;
        destinoMovido = t;
        log("1 tropa de " + origen.getNombre() + " a " + t.getNombre() + ".");
    }

    // ---------------------------------------------------------------- Combate

    private void batalla(Territorio atacante, Territorio defensor) {
        int dadosAtq = Math.min(3, atacante.getTropas() - 1);
        int dadosDef = Math.min(2, defensor.getTropas());
        int[] a = lanzarDados(dadosAtq);
        int[] d = lanzarDados(dadosDef);

        int perdidasAtq = 0, perdidasDef = 0;
        for (int i = 0; i < Math.min(a.length, d.length); i++) {
            if (a[i] > d[i]) perdidasDef++;   // empate gana el defensor
            else perdidasAtq++;
        }
        atacante.agregarTropas(-perdidasAtq);
        defensor.agregarTropas(-perdidasDef);

        log(atacante.getNombre() + " " + Arrays.toString(a) + " vs " + defensor.getNombre() + " "
                + Arrays.toString(d) + ": atacante pierde " + perdidasAtq + ", defensor pierde " + perdidasDef + ".");

        if (defensor.getTropas() == 0) {
            Jugador vencido = defensor.getPropietario();
            int mover = Math.min(dadosAtq, atacante.getTropas() - 1);
            cambiarPropietario(defensor, jugadorActual());
            atacante.agregarTropas(-mover);
            defensor.agregarTropas(mover);
            log("¡" + jugadorActual().getNombre() + " conquista " + defensor.getNombre() + " y mueve " + mover + " tropas!");
            seleccionar(null);

            if (contarTerritorios(vencido) == 0) {
                log(vencido.getNombre() + " ha sido eliminado.");
            }
            comprobarVictoria();
        } else if (atacante.getTropas() < 2) {
            log(atacante.getNombre() + " ya no puede seguir atacando.");
            seleccionar(null);
        }
    }

    // Devuelve los dados ordenados de mayor a menor
    private int[] lanzarDados(int cantidad) {
        int[] dados = new int[cantidad];
        for (int i = 0; i < cantidad; i++) dados[i] = 1 + rnd.nextInt(6);
        Arrays.sort(dados);
        for (int i = 0; i < cantidad / 2; i++) {
            int tmp = dados[i];
            dados[i] = dados[cantidad - 1 - i];
            dados[cantidad - 1 - i] = tmp;
        }
        return dados;
    }

    private void comprobarVictoria() {
        if (contarTerritorios(jugadorActual()) == mapa.length) {
            juegoTerminado = true;
            btnConfirmar.setEnabled(false);
            String msg = "¡" + jugadorActual().getNombre() + " conquistó el mundo y gana la partida!";
            log(msg);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, msg, "Fin del juego", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------- Utilidades

    private boolean sonVecinos(Territorio a, Territorio b) {
        int fa = a.getIndice() / COLS, ca = a.getIndice() % COLS;
        int fb = b.getIndice() / COLS, cb = b.getIndice() % COLS;
        return Math.abs(fa - fb) + Math.abs(ca - cb) == 1;
    }

    private void cambiarPropietario(Territorio t, Jugador j) {
        t.setPropietario(j);
        t.setBackground(coloresJugadores[jugadores.indexOf(j)]);
    }

    private void seleccionar(Territorio t) {
        if (origen != null) origen.setSeleccionado(false);
        origen = t;
        if (origen != null) origen.setSeleccionado(true);
    }

    private int contarTerritorios(Jugador j) {
        int n = 0;
        for (Territorio t : mapa) if (t.getPropietario() == j) n++;
        return n;
    }

    private Jugador jugadorActual() {
        return jugadores.get(jugadorActualIndex);
    }

    private void actualizarInfo() {
        Jugador j = jugadorActual();
        lblFase.setText("Fase: " + FASES[faseActual]);
        lblJugador.setText("Turno de: " + j.getNombre() + " (" + contarTerritorios(j) + " territorios)");
        lblJugador.setForeground(coloresJugadores[jugadorActualIndex].darker());
        lblTropas.setText(faseActual == 0 ? "Tropas por colocar: " + tropasDisponibles : " ");

        if (faseActual == 0) {
            lblAyuda.setText("<html>Haz clic en tus territorios para colocar tropas.</html>");
            btnConfirmar.setText("Terminar refuerzos y atacar");
        } else if (faseActual == 1) {
            lblAyuda.setText("<html>Elige un territorio tuyo y luego un vecino enemigo.</html>");
            btnConfirmar.setText("Terminar ataques y movilizar");
        } else {
            lblAyuda.setText("<html>Elige un territorio tuyo y luego un vecino tuyo para mover tropas.</html>");
            btnConfirmar.setText("Terminar turno");
        }
    }

    private void log(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // Acceso para pruebas automáticas
    Territorio[] getMapa() { return mapa; }
    int getFaseActual() { return faseActual; }
    int getTropasDisponibles() { return tropasDisponibles; }
    Jugador getJugadorActual() { return jugadorActual(); }
    boolean isJuegoTerminado() { return juegoTerminado; }
    Territorio getOrigen() { return origen; }
    void clic(Territorio t) { manejarClickTerritorio(t); }
    void siguienteFase() { avanzarFase(); }
}

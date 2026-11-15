import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Territorio: casilla del mapa. Muestra nombre, inicial del dueño y tropas.
 * Guarda su posición (índice) para saber quiénes son sus vecinos.
 */
public class Territorio extends JButton {
    private static final Color BORDE_NORMAL = new Color(90, 90, 90);
    private static final Color BORDE_SELECCION = Color.WHITE;

    private final String nombre;
    private final int indice;
    private Jugador propietario;
    private int tropas;

    private boolean hover = false;

    public Territorio(String nombre, int indice) {
        this.nombre = nombre;
        this.indice = indice;
        this.tropas = 0;
        this.propietario = null;

        setFont(new Font("Segoe UI", Font.PLAIN, 11));
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setOpaque(true);
        setContentAreaFilled(true);
        setFocusPainted(false);
        setBackground(new Color(210, 210, 210));
        setSeleccionado(false);
        setText(buildText());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    private String buildText() {
        String owner = (propietario == null) ? "-" : propietario.getNombre().substring(propietario.getNombre().length() - 1);
        return "<html><center>" + nombre + "<br/>J" + owner + " · " + tropas + "</center></html>";
    }

    public void setPropietario(Jugador j) {
        this.propietario = j;
        setText(buildText());
    }

    public Jugador getPropietario() {
        return propietario;
    }

    // Suma (o resta, con valor negativo) tropas al territorio
    public void agregarTropas(int c) {
        tropas = Math.max(0, tropas + c);
        setText(buildText());
    }

    public int getTropas() {
        return tropas;
    }

    public String getNombre() {
        return nombre;
    }

    public int getIndice() {
        return indice;
    }

    public void setSeleccionado(boolean seleccionado) {
        setBorder(seleccionado
                ? BorderFactory.createLineBorder(BORDE_SELECCION, 3)
                : BorderFactory.createLineBorder(BORDE_NORMAL, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hover) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new Color(255, 255, 255, 40));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}

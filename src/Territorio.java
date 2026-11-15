import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Territorio: botón estilizado que muestra nombre, propietario inicial y tropas.
 * - No cambia lógica de tu proyecto: sigue siendo un JButton con getters/setters simples.
 */
public class Territorio extends JButton {
    private String nombre;
    private Jugador propietario;
    private int tropas;

    private boolean hover = false;

    public Territorio(String nombre) {
        this.nombre = nombre;
        this.tropas = 0;
        this.propietario = null;

        setFont(new Font("Segoe UI", Font.PLAIN, 11));
        setText(buildText());
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);

        setOpaque(true);
        setContentAreaFilled(true);
        setFocusPainted(false);
        setBorder(BorderFactory.createLineBorder(new Color(90,90,90), 1));

        // Hover visual
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
        String owner = (propietario == null) ? "-" : propietario.getNombre().substring(0,1);
        return "<html><center>" + nombre + "<br/>(" + owner + "/" + tropas + ")</center></html>";
    }

    public void setPropietario(Jugador j) {
        this.propietario = j;
        setText(buildText());
    }

    public Jugador getPropietario() {
        return propietario;
    }

    public void agregarTropas(int c) {
        tropas += c;
        setText(buildText());
    }

    public int getTropas() {
        return tropas;
    }

    public String getNombre() {
        return nombre;
    }

    // Pintado adicional para sombra / hover
    @Override
    protected void paintComponent(Graphics g) {
        if (hover) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new Color(255,255,255,30));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.dispose();
        }
        super.paintComponent(g);
    }
}

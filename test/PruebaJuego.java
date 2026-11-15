import java.util.*;

/**
 * Prueba automática: juega 20 partidas completas con 4 jugadores simulados
 * y comprueba en cada turno que se respeten las reglas del juego.
 */
public class PruebaJuego {
    static int vecino(int a, int b){int fa=a/10,ca=a%10,fb=b/10,cb=b%10;return Math.abs(fa-fb)+Math.abs(ca-cb);}
    static void check(boolean c, String m){ if(!c) throw new RuntimeException("FALLO: "+m); }

    public static void main(String[] args) {
        int partidas = 0, turnosTotales = 0;
        for (long seed = 1; seed <= 20; seed++) {
            List<Jugador> js = new ArrayList<>();
            for (int i = 1; i <= 4; i++) js.add(new Jugador("Jugador " + i));
            PanelJuego p = new PanelJuego(js, new Random(seed));
            Territorio[] m = p.getMapa();
            Random bot = new Random(seed * 31);

            // Reparto inicial: 25 territorios por jugador, 1 tropa cada uno
            for (Jugador j : js) check(Arrays.stream(m).filter(t -> t.getPropietario() == j).count() == 25, "reparto inicial");

            int turnos = 0;
            while (!p.isJuegoTerminado() && turnos < 3000) {
                Jugador yo = p.getJugadorActual();
                check(p.getFaseActual() == 0, "empieza en refuerzos");
                int esperado = Math.max(3, (int) Arrays.stream(m).filter(t -> t.getPropietario() == yo).count() / 3);
                check(p.getTropasDisponibles() == esperado, "cantidad de refuerzos");

                // No puede pasar a ataque sin colocar
                p.siguienteFase(); check(p.getFaseActual() == 0, "bloqueo sin colocar");
                // Clic en territorio enemigo no coloca
                Territorio enemigo = Arrays.stream(m).filter(t -> t.getPropietario() != yo).findFirst().get();
                int antes = enemigo.getTropas(); p.clic(enemigo); check(enemigo.getTropas() == antes && p.getTropasDisponibles()==esperado, "no reforzar enemigo");

                // Colocar en territorios de frontera
                List<Territorio> frontera = new ArrayList<>();
                for (Territorio t : m) if (t.getPropietario()==yo) for (Territorio o : m) if (vecino(t.getIndice(),o.getIndice())==1 && o.getPropietario()!=yo) { frontera.add(t); break; }
                Territorio base = frontera.get(bot.nextInt(frontera.size()));
                while (p.getTropasDisponibles() > 0) p.clic(base);
                p.siguienteFase(); check(p.getFaseActual() == 1, "pasa a ataque");

                // Atacar: varias rondas
                for (int r = 0; r < 40 && !p.isJuegoTerminado(); r++) {
                    Territorio org = null, dst = null;
                    for (Territorio t : m) if (t.getPropietario()==yo && t.getTropas()>=3) for (Territorio o : m)
                        if (vecino(t.getIndice(),o.getIndice())==1 && o.getPropietario()!=yo && t.getTropas()>o.getTropas()) { org=t; dst=o; break; }
                    if (org == null) break;
                    int totalAntes = org.getTropas() + dst.getTropas();
                    Jugador dueno = dst.getPropietario();
                    if (p.getOrigen() != org) p.clic(org); check(p.getOrigen()==org, "selecciona origen"); p.clic(dst);
                    int totalDespues = org.getTropas() + dst.getTropas();
                    check(totalDespues < totalAntes, "la batalla quita tropas");
                    check(org.getTropas() >= 1 && dst.getTropas() >= 1, "nadie queda en 0 tras batalla");
                    if (dst.getPropietario() == yo) check(dueno != yo, "conquista válida");
                    // Atacar territorio no vecino no hace nada
                }
                if (p.isJuegoTerminado()) break;
                p.siguienteFase(); check(p.getFaseActual() == 2, "pasa a movilización");

                // Movilizar: un par origen-destino
                Territorio mo = null, md = null;
                for (Territorio t : m) if (t.getPropietario()==yo && t.getTropas()>=2) for (Territorio o : m)
                    if (vecino(t.getIndice(),o.getIndice())==1 && o.getPropietario()==yo) { mo=t; md=o; break; }
                if (mo != null) {
                    int s = mo.getTropas()+md.getTropas();
                    p.clic(mo); check(p.getOrigen()==mo, "origen mov"); p.clic(md); p.clic(md);
                    check(mo.getTropas()+md.getTropas()==s && mo.getTropas()>=1, "movilización conserva tropas");
                    // Un segundo destino distinto debe rechazarse
                    for (Territorio o : m) if (o!=md && o.getPropietario()==yo && vecino(mo.getIndice(),o.getIndice())==1) { int x=o.getTropas(); p.clic(o); check(o.getTropas()==x || o==mo, "solo un movimiento"); break; }
                }
                p.siguienteFase();
                turnos++;

                for (Territorio t : m) { check(t.getPropietario()!=null, "todo tiene dueño"); check(t.getTropas()>=1, "mínimo 1 tropa: "+t.getNombre()); }
                check(p.getJugadorActual()!=yo || p.isJuegoTerminado() || Arrays.stream(m).allMatch(t->t.getPropietario()==yo), "cambia turno");
                Jugador sig = p.getJugadorActual();
                check(Arrays.stream(m).anyMatch(t->t.getPropietario()==sig), "no le toca a un eliminado");
            }
            check(p.isJuegoTerminado(), "la partida termina (seed "+seed+")");
            Jugador g = p.getJugadorActual();
            check(Arrays.stream(m).allMatch(t -> t.getPropietario() == g), "ganador tiene todo");
            partidas++; turnosTotales += turnos;
        }
        System.out.println("OK: " + partidas + " partidas completas, promedio " + (turnosTotales/partidas) + " turnos");
    }
}

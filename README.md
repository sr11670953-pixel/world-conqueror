# World's Greatest Conqueror

Juego de estrategia por turnos para 4 jugadores, inspirado en Risk, con interfaz gráfica en Java Swing. Gana quien conquiste los 100 territorios del mapa.

## Cómo se juega

Al empezar, el mapa de 10 × 10 se reparte al azar: cada jugador recibe 25 territorios con 1 tropa. Cada turno tiene tres fases:

1. **Refuerzos.** Recibes tantas tropas como tus territorios divididos entre 3 (mínimo 3). Haz clic en tus territorios para colocarlas; no puedes avanzar hasta colocarlas todas.
2. **Ataque.** Elige un territorio tuyo con 2 o más tropas y luego un vecino enemigo (arriba, abajo, izquierda o derecha). Cada clic es una batalla con dados: el atacante lanza hasta 3 y el defensor hasta 2; se comparan de mayor a menor y el empate lo gana el defensor. Si el defensor se queda sin tropas, conquistas el territorio y mueves tropas a él.
3. **Movilización.** Elige un territorio tuyo y luego un vecino tuyo; cada clic mueve 1 tropa. Solo puedes hacer un movimiento por turno.

Los jugadores que pierden todos sus territorios quedan eliminados. El registro de eventos de la derecha muestra cada dado lanzado y cada conquista.

## Cómo está organizado

| Archivo | Qué hace |
|---|---|
| `src/Main.java` | Arranca la aplicación |
| `src/Game.java` | Crea los jugadores y la ventana |
| `src/PanelJuego.java` | Mapa, controles y toda la lógica: turnos, refuerzos, batallas con dados, movilización y victoria |
| `src/Territorio.java` | Casilla del mapa: dueño, tropas, posición y resaltado al seleccionarla |
| `src/Jugador.java` | Datos de cada jugador |
| `test/PruebaJuego.java` | Prueba automática que juega 20 partidas completas y verifica las reglas en cada turno |

## Cómo ejecutarlo

Necesitas Java 11 o superior.

```bash
javac -d out src/*.java
java -cp out Main
```

También puedes abrir la carpeta en VS Code con la extensión de Java y ejecutar `Main.java`.

## Pruebas

```bash
javac -d out src/*.java test/*.java
java -Djava.awt.headless=true -cp out PruebaJuego
```

Juega 20 partidas con jugadores simulados y comprueba, entre otras cosas, que los refuerzos sean los correctos, que ningún territorio quede sin tropas, que las batallas siempre resten tropas, que solo se permita un movimiento por turno y que cada partida termine con un único ganador.

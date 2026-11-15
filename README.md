# World's Greatest Conqueror

Juego de estrategia por turnos, inspirado en Risk, con interfaz gráfica en Java Swing. Cuatro jugadores comparten un mapa de 10 × 10 territorios y avanzan por las fases de cada turno.

> **Estado: en desarrollo.** El mapa, los refuerzos, el cambio de fase y de turno y el registro de eventos funcionan. Las fases de ataque y movilización todavía no están implementadas.

## Qué hace

- Mapa de 100 territorios. Cada jugador empieza con uno al azar y su propio color.
- Cada territorio muestra su dueño y sus tropas. En la fase de refuerzos, haces clic en tus territorios para sumar tropas.
- Turnos con tres fases: refuerzos, ataque y movilización.
- Panel lateral con el jugador y la fase actuales, y un registro de todo lo que pasa.

## Cómo está organizado

| Clase | Qué hace |
|---|---|
| `Main` | Arranca la aplicación |
| `Game` | Crea los jugadores y la ventana |
| `PanelJuego` | Mapa, panel de información, controles y lógica de turnos |
| `Territorio` | Casilla del mapa: dueño, tropas y efecto al pasar el mouse |
| `Jugador` | Datos de cada jugador |

## Cómo ejecutarlo

Necesitas Java 11 o superior.

```bash
cd src
javac *.java
java Main
```

También puedes abrir la carpeta en VS Code con la extensión de Java y ejecutar `Main.java`.

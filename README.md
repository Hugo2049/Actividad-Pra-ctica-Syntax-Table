# Actividad Práctica: FIRST, FOLLOW y Tabla de Análisis Sintáctico
Diseño de Lenguajes de Programación — Abril 2026

Video de demostración: https://youtu.be/bjAKptpoCN0

Repositorio: https://github.com/Hugo2049/Actividad-Pra-ctica-Syntax-Table

---

## Estructura del proyecto

```
proyecto/
├── Problema1/
│   ├── First.java
│   ├── Follow.java
│   ├── GrammarLoader.java
│   ├── gramatica1.txt
│   ├── gramatica2.txt
│   └── gramatica3.txt
├── Problema2/
   ├── TableBuilder.java
   └── TableDisplay.java

```

---

## Problema 1

Implementacion de las funciones FIRST y FOLLOW en tres archivos:

- First.java: cada regla del algoritmo como un metodo separado, calculo global con punto fijo iterativo.
- Follow.java: misma estructura, usa los FIRST ya calculados.
- GrammarLoader.java: lee los archivos de gramatica, coordina el calculo y muestra los resultados en consola. Genera un archivo resultado.txt por cada gramatica procesada.

Gramaticas probadas: expresiones aritmeticas (vista en clase), gramatica con multiples no terminales anulables, y gramatica con simbolos opcionales encadenados.

---

## Problema 2

Implementacion de la tabla de analisis sintactico predictivo en dos archivos:

- TableBuilder.java: construye la tabla aplicando las dos reglas del algoritmo. Detecta conflictos cuando una celda recibe mas de una produccion.
- TableDisplay.java: muestra la tabla formateada en consola y reporta si la gramatica es LL(1).

Reutiliza directamente las clases del Problema 1.

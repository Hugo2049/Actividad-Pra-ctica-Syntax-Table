import java.io.*;
import java.util.*;


public class TableDisplay {

    // ----------------------------------------------------------
    //  Mostrar tabla y resultados
    // ----------------------------------------------------------
    public static void mostrar(TableBuilder builder) {
        GrammarLoader loader = builder.getLoader();
        List<String> ordenNT  = loader.getOrdenNT();
        List<String> terminales = builder.getTerminalesOrdenados();
        Map<String, Map<String, List<List<String>>>> tabla = builder.getTabla();

        // --- Gramática ---
        System.out.println("\n--- Gramática ---");
        System.out.println("    Símbolo inicial: " + loader.getSimboloInicial());
        for (String nt : ordenNT) {
            List<List<String>> prods = loader.getGramatica().get(nt);
            for (int i = 0; i < prods.size(); i++) {
                if (i == 0) System.out.printf("    %-6s ->  %s%n", nt, String.join(" ", prods.get(i)));
                else         System.out.printf("    %-6s  |  %s%n", "", String.join(" ", prods.get(i)));
            }
        }

        // --- FIRST y FOLLOW ---
        System.out.println("\n--- Conjuntos FIRST ---");
        for (String nt : ordenNT) {
            System.out.printf("    FIRST(%-4s) = { %s }%n",
                    nt, String.join(", ", builder.getFirstSets().get(nt)));
        }
        System.out.println("\n--- Conjuntos FOLLOW ---");
        for (String nt : ordenNT) {
            System.out.printf("    FOLLOW(%-4s) = { %s }%n",
                    nt, String.join(", ", builder.getFollowSets().get(nt)));
        }

        // --- Tabla LL(1) ---
        System.out.println("\n--- Tabla de Análisis Sintáctico Predictivo ---\n");

        if (terminales.isEmpty()) {
            System.out.println("    (La tabla está vacía — revisa la gramática)");
            return;
        }

        // Calcular anchos de columna
        int anchoNT = 5;
        for (String nt : ordenNT) anchoNT = Math.max(anchoNT, nt.length() + 2);

        int anchoCol = 8;
        for (String t : terminales) anchoCol = Math.max(anchoCol, t.length() + 2);
        for (String nt : ordenNT) {
            for (String t : terminales) {
                List<List<String>> prods = tabla.get(nt).get(t);
                if (prods != null && !prods.isEmpty()) {
                    String celda = nt + "→" + String.join(" ", prods.get(0));
                    anchoCol = Math.max(anchoCol, celda.length() + 2);
                }
            }
        }

        // Encabezado
        System.out.printf("%-" + anchoNT + "s", "NT \\ T");
        for (String t : terminales) {
            System.out.printf("%-" + anchoCol + "s", t);
        }
        System.out.println();
        System.out.println("-".repeat(anchoNT + anchoCol * terminales.size()));

        // Filas
        for (String nt : ordenNT) {
            System.out.printf("%-" + anchoNT + "s", nt);
            for (String t : terminales) {
                List<List<String>> prods = tabla.get(nt).get(t);
                String celda;
                if (prods == null || prods.isEmpty()) {
                    celda = "";                                    // Error de sincronización
                } else if (prods.size() == 1) {
                    celda = nt + "→" + String.join(" ", prods.get(0));
                } else {
                    celda = "** CONFLICTO **";                     // No es LL(1)
                }
                System.out.printf("%-" + anchoCol + "s", celda);
            }
            System.out.println();
        }

        System.out.println("-".repeat(anchoNT + anchoCol * terminales.size()));

        // --- Resultado LL(1) ---
        System.out.println();
        if (builder.esLL1()) {
            System.out.println("  RESULTADO: La gramatica ES LL(1) - No hay conflictos en la tabla.");
        } else {
            System.out.println("  RESULTADO: La gramatica NO ES LL(1) - Se detectaron conflictos:\n");
            Map<String, List<List<String>>> conflictos = builder.obtenerConflictos();
            for (Map.Entry<String, List<List<String>>> entrada : conflictos.entrySet()) {
                System.out.println("  Conflicto en " + entrada.getKey() + ":");
                String nt = entrada.getKey().replaceAll("M\\[(.+), .+\\]", "$1");
                for (List<String> p : entrada.getValue()) {
                    System.out.println("      " + nt + " -> " + String.join(" ", p));
                }
            }
        }
    }

    // ----------------------------------------------------------
    //  Main — Punto de entrada
    // ----------------------------------------------------------
    public static void main(String[] args) throws IOException {
        // Rutas por defecto: archivos en la carpeta Problema1
        // (relativas a la carpeta Problema2 desde donde se corre)
        String[] archivos;
        if (args.length > 0) {
            archivos = args;
        } else {
            archivos = new String[]{
                "../Problema1/gramatica1.txt",
                "../Problema1/gramatica2.txt",
                "../Problema1/gramatica3.txt"
            };
        }

        for (String archivo : archivos) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("  Procesando: " + archivo);
            System.out.println("=".repeat(60));

            GrammarLoader loader = new GrammarLoader();
            try {
                loader.cargar(archivo);
            } catch (FileNotFoundException e) {
                System.out.println("  ERROR: No se encontro el archivo: " + archivo);
                continue;
            }

            TableBuilder builder = new TableBuilder(loader);
            builder.construir();
            mostrar(builder);
        }
    }
}

import java.io.*;
import java.util.*;


public class GrammarLoader {

    // Representación interna de la gramática
    private Map<String, List<List<String>>> gramatica;   // NT → lista de producciones
    private LinkedHashSet<String> noTerminales;           // Orden de inserción preservado
    private List<String> ordenNT;                         // Para impresión ordenada
    private String simboloInicial;

    public GrammarLoader() {
        gramatica     = new LinkedHashMap<>();
        noTerminales  = new LinkedHashSet<>();
        ordenNT       = new ArrayList<>();
        simboloInicial = null;
    }

    // ----------------------------------------------------------
    //  Lectura del archivo de gramática
    // ----------------------------------------------------------
    public void cargar(String rutaArchivo) throws IOException {
        // Limpiar estado anterior
        gramatica.clear();
        noTerminales.clear();
        ordenNT.clear();
        simboloInicial = null;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaArchivo), "UTF-8"))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                // Saltar líneas vacías y comentarios
                if (linea.isEmpty() || linea.startsWith("#")) continue;

                // Línea START: para símbolo inicial explícito
                if (linea.startsWith("START:")) {
                    simboloInicial = linea.substring(6).trim();
                    continue;
                }

                // Debe tener ->
                if (!linea.contains("->")) continue;

                // Separar lado izquierdo y derecho
                String[] partes = linea.split("->", 2);
                String lhs = partes[0].trim();
                String rhs = partes[1].trim();

                // Registrar no terminal (si es nuevo)
                if (!noTerminales.contains(lhs)) {
                    noTerminales.add(lhs);
                    ordenNT.add(lhs);
                    gramatica.put(lhs, new ArrayList<>());
                }

                // Separar alternativas por |  y parsear cada una
                String[] alternativas = rhs.split("\\|");
                for (String alt : alternativas) {
                    alt = alt.trim();
                    if (alt.isEmpty()) continue;

                    String[] tokens = alt.split("\\s+");
                    List<String> simbolos = new ArrayList<>(Arrays.asList(tokens));
                    gramatica.get(lhs).add(simbolos);
                }
            }
        }

        // Si no se especificó START, usar el primer no terminal
        if (simboloInicial == null && !ordenNT.isEmpty()) {
            simboloInicial = ordenNT.get(0);
        }
    }

    // ----------------------------------------------------------
    //  Cálculo y visualización
    // ----------------------------------------------------------
    public void calcularYMostrar(String rutaSalida) throws IOException {
        // --- Calcular FIRST y FOLLOW ---
        Map<String, Set<String>> firstSets = First.calcularTodo(gramatica, noTerminales);
        Map<String, Set<String>> followSets = Follow.calcularTodo(
                gramatica, noTerminales, simboloInicial, firstSets);

        // --- Construir texto de salida ---
        StringBuilder sb = new StringBuilder();

        sb.append("=== GRAMÁTICA ===\n");
        sb.append("Símbolo inicial: ").append(simboloInicial).append("\n\n");
        for (String nt : ordenNT) {
            List<List<String>> prods = gramatica.get(nt);
            for (int i = 0; i < prods.size(); i++) {
                if (i == 0) sb.append(String.format("  %-6s ->  ", nt));
                else        sb.append(String.format("  %-6s  |  ", ""));
                sb.append(String.join(" ", prods.get(i))).append("\n");
            }
        }

        sb.append("\n=== CONJUNTOS FIRST ===\n");
        for (String nt : ordenNT) {
            sb.append(String.format("  FIRST(%-4s) = { %s }\n",
                    nt, String.join(", ", firstSets.get(nt))));
        }

        sb.append("\n=== CONJUNTOS FOLLOW ===\n");
        for (String nt : ordenNT) {
            sb.append(String.format("  FOLLOW(%-4s) = { %s }\n",
                    nt, String.join(", ", followSets.get(nt))));
        }

        // --- Mostrar en consola ---
        System.out.println(sb);

        // --- Guardar en archivo (para Problema 2) ---
        if (rutaSalida != null) {
            guardarResultado(rutaSalida, firstSets, followSets);
            System.out.println("  [Resultado guardado en: " + rutaSalida + "]");
        }
    }


    private void guardarResultado(String rutaArchivo,
                                   Map<String, Set<String>> firstSets,
                                   Map<String, Set<String>> followSets) throws IOException {

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(rutaArchivo), "UTF-8"))) {

            // Sección 1: La gramática (para que P2 la re-cargue si necesita)
            pw.println("# Archivo generado automáticamente por GrammarLoader (Problema 1)");
            pw.println("START: " + simboloInicial);

            for (String nt : ordenNT) {
                List<List<String>> prods = gramatica.get(nt);
                StringBuilder linea = new StringBuilder(nt + " -> ");
                for (int i = 0; i < prods.size(); i++) {
                    if (i > 0) linea.append(" | ");
                    linea.append(String.join(" ", prods.get(i)));
                }
                pw.println(linea);
            }

            // Sección 2: Resultados FIRST
            pw.println("---FIRST---");
            for (String nt : ordenNT) {
                pw.println("FIRST(" + nt + ")=" + String.join(",", firstSets.get(nt)));
            }

            // Sección 3: Resultados FOLLOW
            pw.println("---FOLLOW---");
            for (String nt : ordenNT) {
                pw.println("FOLLOW(" + nt + ")=" + String.join(",", followSets.get(nt)));
            }
        }
    }


    public Map<String, List<List<String>>> getGramatica()  { return gramatica; }
    public Set<String>  getNoTerminales()                  { return noTerminales; }
    public List<String> getOrdenNT()                       { return ordenNT; }
    public String       getSimboloInicial()                { return simboloInicial; }

  
    public static void main(String[] args) throws IOException {
        // Si no se pasan argumentos, procesar las 3 gramáticas por defecto
        String[] archivos;
        if (args.length > 0) {
            archivos = args;
        } else {
            archivos = new String[]{
                "gramatica1.txt",
                "gramatica2.txt",
                "gramatica3.txt"
            };
        }

        for (String archivo : archivos) {
            System.out.println("\n" + "=".repeat(55));
            System.out.println("  Procesando: " + archivo);
            System.out.println("=".repeat(55));

            GrammarLoader loader = new GrammarLoader();
            try {
                loader.cargar(archivo);
                String salida = archivo.replace(".txt", "_resultado.txt");
                loader.calcularYMostrar(salida);
            } catch (FileNotFoundException e) {
                System.out.println("  ERROR: No se encontró el archivo: " + archivo);
            }
        }
    }
}

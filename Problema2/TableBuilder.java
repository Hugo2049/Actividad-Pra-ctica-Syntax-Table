import java.util.*;

public class TableBuilder {

    private GrammarLoader loader;
    private Map<String, Set<String>> firstSets;
    private Map<String, Set<String>> followSets;

    // M[noTerminal][terminal] → lista de producciones
    // (lista de tamaño > 1 indica conflicto LL(1))
    private Map<String, Map<String, List<List<String>>>> tabla;

    // Terminales encontrados durante la construcción (orden de aparición)
    private List<String> terminalesOrdenados;

    // ----------------------------------------------------------
    //  Constructor
    // ----------------------------------------------------------
    public TableBuilder(GrammarLoader loader) {
        this.loader = loader;

        // Calcular FIRST y FOLLOW usando las clases del Problema 1
        this.firstSets = First.calcularTodo(
                loader.getGramatica(), loader.getNoTerminales());
        this.followSets = Follow.calcularTodo(
                loader.getGramatica(), loader.getNoTerminales(),
                loader.getSimboloInicial(), firstSets);

        // Inicializar tabla vacía
        this.tabla = new LinkedHashMap<>();
        for (String nt : loader.getNoTerminales()) {
            tabla.put(nt, new LinkedHashMap<>());
        }
        this.terminalesOrdenados = new ArrayList<>();
    }

    // ----------------------------------------------------------
    //  Construcción de la tabla
    // ----------------------------------------------------------
    public void construir() {
        Set<String> terminalesSet = new LinkedHashSet<>();
        Map<String, List<List<String>>> gramatica = loader.getGramatica();
        Set<String> noTerminales = loader.getNoTerminales();

        for (String A : loader.getOrdenNT()) {
            List<List<String>> producciones = gramatica.getOrDefault(A, Collections.emptyList());

            for (List<String> produccion : producciones) {

                // Calcular FIRST(α)
                Set<String> firstAlpha = calcularFirstProduccion(produccion, noTerminales);

                // PASO 1: Para cada terminal a en FIRST(α) - {ε}
                for (String a : firstAlpha) {
                    if (!a.equals("ε")) {
                        paso1_agregarPorFirst(A, a, produccion);
                        terminalesSet.add(a);
                    }
                }

                // PASO 2: Si ε ∈ FIRST(α), usar FOLLOW(A)
                if (firstAlpha.contains("ε")) {
                    for (String b : followSets.get(A)) {
                        paso2_agregarPorFollow(A, b, produccion);
                        terminalesSet.add(b);
                    }
                }
            }
        }

        // Ordenar terminales con $ siempre al final
        for (String t : terminalesSet) {
            if (!t.equals("$")) terminalesOrdenados.add(t);
        }
        terminalesOrdenados.add("$");
    }

 
    private Set<String> calcularFirstProduccion(List<String> produccion,
                                                  Set<String> noTerminales) {
        if (produccion.size() == 1 && produccion.get(0).equals("ε")) {
            Set<String> solo = new LinkedHashSet<>();
            solo.add("ε");
            return solo;
        }
        return First.firstDeSecuencia(produccion, firstSets, noTerminales);
    }

    /**
     * PASO 1: Agrega A → produccion a M[A, terminal].
     */
    private void paso1_agregarPorFirst(String A, String terminal,
                                        List<String> produccion) {
        agregarACelda(A, terminal, produccion);
    }

    /**
     * PASO 2: Agrega A → produccion a M[A, terminal] (venía de FOLLOW).
     */
    private void paso2_agregarPorFollow(String A, String terminal,
                                         List<String> produccion) {
        agregarACelda(A, terminal, produccion);
    }

    /** Inserta una producción en la celda M[nt][terminal] */
    private void agregarACelda(String nt, String terminal,
                                List<String> produccion) {
        Map<String, List<List<String>>> fila = tabla.get(nt);
        if (!fila.containsKey(terminal)) {
            fila.put(terminal, new ArrayList<>());
        }
        // Evitar duplicados exactos
        for (List<String> p : fila.get(terminal)) {
            if (p.equals(produccion)) return;
        }
        fila.get(terminal).add(new ArrayList<>(produccion));
    }

    // ----------------------------------------------------------
    //  Verificación LL(1)
    // ----------------------------------------------------------
    public boolean esLL1() {
        for (String nt : loader.getNoTerminales()) {
            for (List<List<String>> prods : tabla.get(nt).values()) {
                if (prods.size() > 1) return false;
            }
        }
        return true;
    }


    public Map<String, List<List<String>>> obtenerConflictos() {
        Map<String, List<List<String>>> conflictos = new LinkedHashMap<>();
        for (String nt : loader.getOrdenNT()) {
            for (String t : terminalesOrdenados) {
                List<List<String>> prods = tabla.get(nt).get(t);
                if (prods != null && prods.size() > 1) {
                    conflictos.put("M[" + nt + ", " + t + "]", prods);
                }
            }
        }
        return conflictos;
    }

  
    public Map<String, Map<String, List<List<String>>>> getTabla()     { return tabla; }
    public List<String>            getTerminalesOrdenados()             { return terminalesOrdenados; }
    public Map<String, Set<String>> getFirstSets()                      { return firstSets; }
    public Map<String, Set<String>> getFollowSets()                     { return followSets; }
    public GrammarLoader            getLoader()                         { return loader; }
}

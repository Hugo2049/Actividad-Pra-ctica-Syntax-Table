import java.util.*;


public class First {

    // ----------------------------------------------------------
    //  Regla 1: Verificar si un símbolo es terminal
    // ----------------------------------------------------------
    public static boolean R1_esTerminal(String simbolo, Set<String> noTerminales) {
        return !noTerminales.contains(simbolo);
    }

    // ----------------------------------------------------------
    //  Regla 2: Producción épsilon
    // ----------------------------------------------------------
    public static boolean R2_esProduccionEpsilon(List<String> produccion) {
        return produccion.size() == 1 && produccion.get(0).equals("ε");
    }

    // ----------------------------------------------------------
    //  Regla 3: Secuencia Y1 Y2 ... Yk
    // ----------------------------------------------------------
    public static Set<String> R3_firstDeSecuencia(
            List<String> simbolos,
            Map<String, Set<String>> firstSets,
            Set<String> noTerminales) {

        Set<String> resultado = new LinkedHashSet<>();
        boolean todosAnulables = true;

        for (String sym : simbolos) {
            // Obtener FIRST del símbolo individual (terminal o no terminal)
            Set<String> firstSym = obtenerFirstSimboloIndividual(sym, firstSets, noTerminales);

            // Agregar FIRST(sym) - {ε}
            for (String s : firstSym) {
                if (!s.equals("ε")) resultado.add(s);
            }

            // Si ε ∉ FIRST(sym) → la cadena no puede ser vacía a partir de aquí
            if (!firstSym.contains("ε")) {
                todosAnulables = false;
                break;
            }
        }

        // Si todos los símbolos pueden derivar ε → ε ∈ FIRST(secuencia)
        if (todosAnulables) {
            resultado.add("ε");
        }

        return resultado;
    }

    // ----------------------------------------------------------
    //  FIRST de un símbolo individual
    // ----------------------------------------------------------
    public static Set<String> obtenerFirstSimboloIndividual(
            String sym,
            Map<String, Set<String>> firstSets,
            Set<String> noTerminales) {

        Set<String> resultado = new LinkedHashSet<>();

        if (sym.equals("ε")) {
            // El símbolo es directamente épsilon
            resultado.add("ε");
        } else if (R1_esTerminal(sym, noTerminales)) {
            // R1: terminal → FIRST(sym) = {sym}
            resultado.add(sym);
        } else {
            // No terminal → usar conjunto ya calculado (puede ser parcial)
            resultado.addAll(firstSets.getOrDefault(sym, new LinkedHashSet<>()));
        }

        return resultado;
    }

    // ----------------------------------------------------------
    //  Cálculo global con punto fijo iterativo
    // ----------------------------------------------------------
    public static Map<String, Set<String>> calcularTodo(
            Map<String, List<List<String>>> gramatica,
            Set<String> noTerminales) {

        // Inicializar todos los conjuntos FIRST como vacíos
        Map<String, Set<String>> firstSets = new LinkedHashMap<>();
        for (String nt : noTerminales) {
            firstSets.put(nt, new LinkedHashSet<>());
        }

        // Iterar hasta punto fijo (ningún conjunto cambia)
        boolean cambio = true;
        while (cambio) {
            cambio = false;

            for (String A : noTerminales) {
                Set<String> firstA = firstSets.get(A);
                int tamañoAntes = firstA.size();

                List<List<String>> producciones = gramatica.getOrDefault(A, Collections.emptyList());
                for (List<String> produccion : producciones) {

                    if (R2_esProduccionEpsilon(produccion)) {
                        // R2: A → ε
                        firstA.add("ε");
                    } else {
                        // R3: A → Y1 Y2 ... Yk
                        Set<String> aAgregar = R3_firstDeSecuencia(produccion, firstSets, noTerminales);
                        firstA.addAll(aAgregar);
                    }
                }

                // ¿Cambió el conjunto?
                if (firstA.size() != tamañoAntes) {
                    cambio = true;
                }
            }
        }

        return firstSets;
    }


    public static Set<String> firstDeSecuencia(
            List<String> secuencia,
            Map<String, Set<String>> firstSets,
            Set<String> noTerminales) {
        return R3_firstDeSecuencia(secuencia, firstSets, noTerminales);
    }
}

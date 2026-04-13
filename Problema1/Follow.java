import java.util.*;

public class Follow {

    // ----------------------------------------------------------
    //  Regla 1: Símbolo inicial recibe $
    // ----------------------------------------------------------
    public static void R1_agregarDollar(String simboloInicial,
                                         Map<String, Set<String>> followSets) {
        followSets.get(simboloInicial).add("$");
    }

    // ----------------------------------------------------------
    //  Regla 2: FIRST del sufijo
    // ----------------------------------------------------------
    public static Set<String> R2_firstDelSufijo(
            List<String> sufijo,
            Map<String, Set<String>> firstSets,
            Set<String> noTerminales) {

        Set<String> resultado = new LinkedHashSet<>();
        Set<String> firstSufijo = First.firstDeSecuencia(sufijo, firstSets, noTerminales);

        for (String s : firstSufijo) {
            if (!s.equals("ε")) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    // ----------------------------------------------------------
    //  Regla 3: Propagar FOLLOW del padre
    // ----------------------------------------------------------
    public static boolean R3_debePropagarFollow(
            List<String> sufijo,
            Map<String, Set<String>> firstSets,
            Set<String> noTerminales) {

        // β vacío → B está al final de la producción
        if (sufijo.isEmpty()) return true;

        // ε ∈ FIRST(β) → β puede desaparecer
        Set<String> firstSufijo = First.firstDeSecuencia(sufijo, firstSets, noTerminales);
        return firstSufijo.contains("ε");
    }

    // ----------------------------------------------------------
    //  Cálculo global con punto fijo iterativo
    // ----------------------------------------------------------
    public static Map<String, Set<String>> calcularTodo(
            Map<String, List<List<String>>> gramatica,
            Set<String> noTerminales,
            String simboloInicial,
            Map<String, Set<String>> firstSets) {

        // Inicializar todos los conjuntos FOLLOW como vacíos
        Map<String, Set<String>> followSets = new LinkedHashMap<>();
        for (String nt : noTerminales) {
            followSets.put(nt, new LinkedHashSet<>());
        }

        // R1: $ ∈ FOLLOW(símboloInicial)
        R1_agregarDollar(simboloInicial, followSets);

        // Iterar hasta punto fijo
        boolean cambio = true;
        while (cambio) {
            cambio = false;

            // Para cada producción A → produccion
            for (String A : noTerminales) {
                List<List<String>> producciones = gramatica.getOrDefault(A, Collections.emptyList());

                for (List<String> produccion : producciones) {
                    // Saltar producciones épsilon
                    if (produccion.size() == 1 && produccion.get(0).equals("ε")) continue;

                    // Examinar cada posición i en la producción
                    for (int i = 0; i < produccion.size(); i++) {
                        String B = produccion.get(i);

                        // Solo procesar no terminales
                        if (!noTerminales.contains(B)) continue;

                        Set<String> followB = followSets.get(B);
                        int tamañoAntes = followB.size();

                        // β = sufijo después de B
                        List<String> sufijo = produccion.subList(i + 1, produccion.size());

                        // R2: agregar FIRST(β) - {ε} a FOLLOW(B)
                        Set<String> deR2 = R2_firstDelSufijo(sufijo, firstSets, noTerminales);
                        followB.addAll(deR2);

                        // R3: si ε ∈ FIRST(β) o β vacío → agregar FOLLOW(A) a FOLLOW(B)
                        if (R3_debePropagarFollow(sufijo, firstSets, noTerminales)) {
                            followB.addAll(followSets.get(A));
                        }

                        if (followB.size() != tamañoAntes) {
                            cambio = true;
                        }
                    }
                }
            }
        }

        return followSets;
    }
}

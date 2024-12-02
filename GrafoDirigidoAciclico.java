package proyectoaed;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author jossu
 */
public class GrafoDirigidoAciclico implements Serializable {
    private static final long serialVersionUID = 1L;
    private int n;
    private Map<String, List<String>> listaAdyacencia;
    private boolean[][] matrizAdyacencia;
    private List<String> vertices;

    // Constructor general
    public GrafoDirigidoAciclico(int n, boolean usarLetras, boolean randomVertices) {
        this.n = n;
        this.listaAdyacencia = new HashMap<>();
        this.matrizAdyacencia = new boolean[n][n];
        this.vertices = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String vertex;
            if (randomVertices) {
                if (usarLetras) {
                    vertex = String.valueOf((char) ('A' + (int) (Math.random() * 26)));
                } else {
                    vertex = String.valueOf((int) (Math.random() * 1000));
                }
            } else {
                if (usarLetras) {
                    vertex = String.valueOf((char) ('A' + i));
                } else {
                    vertex = String.valueOf(i);
                }
            }
            vertices.add(vertex);
            listaAdyacencia.put(vertex, new ArrayList<>());
        }
    }

    // Constructor de numeros ordenados
    public GrafoDirigidoAciclico(int n) {
        this(n, false, false);
    }

    // Constructor default
    public GrafoDirigidoAciclico() {
        this(4, false, true);
    }

    // Obtener el grado de entrada de un vertice para su ordenamiento 
    public int gradoDeEntrada(String vertex) {
        if (!vertices.contains(vertex)) {
            throw new IllegalArgumentException("Vértice fuera de rango");
        }
        int gradoEntrada = 0;
        int index = vertices.indexOf(vertex);
        for (int j = 0; j < n; j++) {
            if (matrizAdyacencia[j][index]) {
                gradoEntrada++;
            }
        }
        return gradoEntrada;
    }

    // Obtener el grado de salida de un vertice para su ordenamiento
    public int gradoDeSalida(String vertex) {
        if (!vertices.contains(vertex)) {
            throw new IllegalArgumentException("Vértice fuera de rango");
        }
        return listaAdyacencia.get(vertex).size();
    }

    // regresa el numero de aristas en el grafo
    public int cuantasAristasHay() {
        return listaAdyacencia.values().stream().mapToInt(List::size).sum();
    }

    // verificar adyacencia
    public boolean adyacente(String i, String j) {
        validarVertices(i, j);
        return listaAdyacencia.get(i).contains(j);
    }

    // verificar conexion entre los vertices por medio de dfs
    public boolean conectados(String i, String j) {
        validarVertices(i, j);
        boolean[] visitado = new boolean[vertices.size()];
        return dfsConectado(i, j, visitado);
    }

    // dfs usado para determinar las conexiones 
    private boolean dfsConectado(String i, String j, boolean[] visitado) {
        if (i.equals(j)) return true;
        int indexI = vertices.indexOf(i);
        visitado[indexI] = true;

        for (String vecino : obtenerVecinos(i)) {
            int vecinoIndex = vertices.indexOf(vecino);
            if (!visitado[vecinoIndex]) {
                if (dfsConectado(vecino, j, visitado)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ordenar usando topological sort
    public String topologicalSort() {
        Map<String, Integer> gradosEntrada = new HashMap<>();
        for (String vertex : vertices) {
            gradosEntrada.put(vertex, gradoDeEntrada(vertex));
        }

        Queue<String> cola = new LinkedList<>();
        for (String vertex : vertices) {
            if (gradosEntrada.get(vertex) == 0) {
                cola.add(vertex);
            }
        }

        List<String> ordenTopologico = new ArrayList<>();
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            ordenTopologico.add(actual);

            for (String vecino : obtenerVecinos(actual)) {
                gradosEntrada.put(vecino, gradosEntrada.get(vecino) - 1);
                if (gradosEntrada.get(vecino) == 0) {
                    cola.add(vecino);
                }
            }
        }

        if (ordenTopologico.size() != vertices.size()) {
            throw new IllegalStateException("El grafo tiene ciclos y no se puede realizar el orden topológico.");
        }

        reorganizarVertices(ordenTopologico);
        return String.join("-", ordenTopologico);
    }

    // Verificar si el grafo tiene ciclos con un dfs
    public boolean tieneCiclos() {
        boolean[] visitado = new boolean[vertices.size()];
        boolean[] enRecursion = new boolean[vertices.size()];

        for (String vertex : vertices) {
            if (!visitado[vertices.indexOf(vertex)]) {
                if (tieneCiclosUtil(vertex, visitado, enRecursion)) {
                    return true;
                }
            }
        }
        return false;
    }

    // dfs usado para los ciclos
    private boolean tieneCiclosUtil(String vertex, boolean[] visitado, boolean[] enRecursion) {
        int i = vertices.indexOf(vertex);
        visitado[i] = true;
        enRecursion[i] = true;

        for (String vecino : obtenerVecinos(vertex)) {
            int vecinoIndex = vertices.indexOf(vecino);
            if (!visitado[vecinoIndex] && tieneCiclosUtil(vecino, visitado, enRecursion)) {
                return true;
            } else if (enRecursion[vecinoIndex]) {
                return true;
            }
        }
        enRecursion[i] = false;
        return false;
    }

    // devolver la lista de adyacencia
    public String mostrarEstructura() {
        StringBuilder sb = new StringBuilder();
        for (String vertex : vertices) {
            sb.append(vertex).append(": ").append(listaAdyacencia.get(vertex)).append("\n");
        }
        return sb.toString();
    }

    // insertar arista, una por una
    public boolean insertarArista(String i, String j) {
        validarVertices(i, j);
        if (i.equals(j) || adyacente(i, j)) {
            return false;
        }
        listaAdyacencia.get(i).add(j);
        matrizAdyacencia[vertices.indexOf(i)][vertices.indexOf(j)] = true;

        if (tieneCiclos()) {
            listaAdyacencia.get(i).remove(j);
            matrizAdyacencia[vertices.indexOf(i)][vertices.indexOf(j)] = false;
            return false;
        }
        return true;
    }

    // borrar todas las aristas
    public void eliminarAristas() {
        listaAdyacencia.values().forEach(List::clear);
    }

    // obtener los vecinos para determinar conexiones
    public List<String> obtenerVecinos(String vertex) {
        if (!vertices.contains(vertex)) {
            throw new IllegalArgumentException("Vértice fuera de rango");
        }
        return new ArrayList<>(listaAdyacencia.get(vertex));
    }

    // metodos que reorganizan el grafo partiendo del topical sort
    private void validarVertices(String i, String j) {
        if (!vertices.contains(i) || !vertices.contains(j)) {
            throw new IllegalArgumentException("Vértice fuera de rango");
        }
    }

    private void reorganizarVertices(List<String> nuevoOrden) {
        vertices = new ArrayList<>(nuevoOrden);
        Map<String, List<String>> nuevaListaAdyacencia = new LinkedHashMap<>();
        for (String vertex : vertices) {
            nuevaListaAdyacencia.put(vertex, listaAdyacencia.get(vertex));
        }
        listaAdyacencia = nuevaListaAdyacencia;
    }

    @Override
    public String toString() {
        return "Grafo{" +
               "n=" + n +
               ", vertices=" + vertices +"}"+
               "\n matrizAdyacencia=" + Arrays.deepToString(matrizAdyacencia) +
               '}';
    }

    //getters y setters
    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public Map<String, List<String>> getListaAdyacencia() {
        return listaAdyacencia;
    }

    public void setListaAdyacencia(Map<String, List<String>> listaAdyacencia) {
        this.listaAdyacencia = listaAdyacencia;
    }

    public boolean[][] getMatrizAdyacencia() {
        return matrizAdyacencia;
    }

    public void setMatrizAdyacencia(boolean[][] matrizAdyacencia) {
        this.matrizAdyacencia = matrizAdyacencia;
    }

    public List<String> getVertices() {
        return vertices;
    }

    public void setVertices(List<String> vertices) {
        this.vertices = vertices;
    }
    
    
}

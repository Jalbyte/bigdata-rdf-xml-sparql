package org.example.cine;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.util.FileManager;
import org.apache.jena.datatypes.xsd.XSDDatatype;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CineColombiaManager — Ejemplo de aplicación que construye un modelo RDF
 * sobre una colección de películas, lo persiste en disco y ejecuta un conjunto
 * de consultas SPARQL de ejemplo sobre ese grafo.
 *
 * Objetivos de este archivo:
 * - Servir como ejemplo didáctico de uso de Apache Jena (Model, Resources, Literals).
 * - Mostrar cómo persistir/leer un fichero RDF (RDF/XML abreviado).
 * - Ejecutar consultas SPARQL y presentar resultados en tablas ASCII legibles.
 */
public class CineColombiaManager {

    /** Nombre del fichero RDF en disco (salida/entrada). */
    private static final String RDF_FILE = "cine_colombia_actual.rdf";

    /** Espacio de nombres base para las propiedades del dominio 'cine'. */
    private static final String NS = "http://example.org/cine#";

    /**
     * Punto de entrada de la aplicación.
     * Crea el modelo RDF, lo guarda en disco y luego lo recarga para ejecutar
     * las consultas SPARQL de ejemplo.
     */
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE GESTIÓN CINE COLOMBIA ===\n");

        // 1) Construcción en memoria del grafo RDF con datos de ejemplo
        Model model = createRDFModel();

        // 2) Persistencia del grafo a disco en formato RDF/XML (abreviado)
        saveRDFModel(model);

        // 3) Lectura del grafo desde disco y ejecución de consultas
        Model loadedModel = loadRDFModel();
        if (loadedModel != null) {
            executeDemoQueries(loadedModel);
        }
    }

    /**
     * Crea y devuelve un modelo RDF con un conjunto pequeño de películas de ejemplo.
     * Cada película se representa como un recurso con propiedades sencillas
     * (título, género, duración, clasificación, fecha de estreno, etc.).
     *
     * @return Modelo RDF en memoria con los recursos creados
     */
    private static Model createRDFModel() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("cine", NS);

        /* Definición explícita de las propiedades utilizadas en el dominio.
         * Se crea una Property por cada predicado para clarificar la intención.
         */
        Property titulo = model.createProperty(NS, "titulo");
        Property tituloEspanol = model.createProperty(NS, "tituloEspanol");
        Property genero = model.createProperty(NS, "genero");
        Property duracion = model.createProperty(NS, "duracion");
        Property clasificacion = model.createProperty(NS, "clasificacion");
        Property estreno = model.createProperty(NS, "estreno");
        Property estado = model.createProperty(NS, "estado");
        Property preventa = model.createProperty(NS, "preventa");
        Property formato = model.createProperty(NS, "formato");

        /*
         * Añadimos manualmente una serie de recursos (películas) para poblar el
         * grafo. En un caso real estos datos vendrían de una fuente externa
         * (CSV, API, base de datos, etc.). Aquí se ilustran distintas propiedades
         * y tipos de valores (literales, enteros, booleanos, fechas tipadas).
         */
        createMovieResource(model, "1", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Now You See Me: Now You Don't", "Los Ilusionistas 3", "Thriller", 112, "Mayores de 12 años", "2025-11-13", "En cartelera", true, "Estreno");

        createMovieResource(model, "2", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Wicked: for Good", "Wicked: Por Siempre", "Musical", 138, "Mayores de 12 años", "2025-11-20", "En cartelera", true, "Preventa");

        createMovieResource(model, "3", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Seventeen World Tour [NEW_] In Japan: Live Viewing", "Seventeen World Tour [NEW_] In Japan: Live Viewing",
            "Concierto", 225, "Por confirmar", "2025-11-29", "En cartelera", true, "Estreno");

        createMovieResource(model, "4", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Predator: Badlands", "Depredador: Tierras Salvajes", "Acción", 0, "Mayores de 12 años", "2025-11-06", "En cartelera", false, "Estreno");

        createMovieResource(model, "5", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Twice One in a Million", "Twice One in a Million", "Documental", 121, "Mayores de 7 años", "2025-11-06", "En cartelera", true, "Estreno");

        createMovieResource(model, "6", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Grand Prix of Europe", "El Gran Premio: A Toda Velocidad", "Animación", 98, "Para todo el Público", "2025-11-06", "En cartelera", false, "Estreno");

        createMovieResource(model, "7", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Roofman", "Un Buen Ladrón", "Comedia", 126, "Mayores de 12 años", "2025-11-06", "En cartelera", false, "Estreno");

        createMovieResource(model, "8", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Dollhouse", "Dollhouse: Muñeca Maldita", "Terror", 109, "Exclusiva para Mayores de 15 años", "2025-11-06", "En cartelera", true, "Preventa");

        createMovieResource(model, "9", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Rebbeca: Becky G", "Rebbeca: Becky G", "Musical", 98, "Mayores de 12 años", "2025-12-10", "En cartelera", true, "Preventa");

        createMovieResource(model, "10", titulo, tituloEspanol, genero, duracion, clasificacion, estreno, estado, preventa, formato,
            "Tron: Ares", "Tron: Ares", "Acción", 119, "Mayores de 7 años", "2025-10-09", "En cartelera", false, "Estreno");

        return model;
    }

    /**
     * Helper que crea un recurso 'película' en el modelo.
     *
     * @param model Modelo RDF donde se crea el recurso.
     * @param id Identificador simple para construir la URI del recurso.
     * @param titulo Propiedad RDF para el título original.
     * @param tituloEspanol Propiedad RDF para el título en español.
     * @param genero Propiedad RDF para el/los géneros.
     * @param duracion Propiedad RDF para la duración en minutos (int).
     * @param clasificacion Propiedad RDF para la clasificación por edad.
     * @param estreno Propiedad RDF para la fecha de estreno (xsd:date).
     * @param estado Propiedad RDF para el estado comercial (ej. "En cartelera").
     * @param preventa Propiedad RDF booleana que indica si hay preventa.
     * @param formato Propiedad RDF para el formato o etiqueta comercial.
     * @param title Título original (String literal).
     * @param spanishTitle Título en español (String literal).
     * @param genre Género(s) como String (posible lista separada por comas).
     * @param duration Duración en minutos. Si es 0 se omite el literal de duración.
     * @param rating Clasificación por edad como texto.
     * @param releaseDate Fecha de estreno en formato YYYY-MM-DD (se tipa como xsd:date).
     * @param status Estado comercial de la película.
     * @param preSale Booleano que indica si tiene preventa.
     * @param format Etiqueta de formato (e.g. "Estreno", "Preventa").
     */
    private static void createMovieResource(Model model, String id, Property titulo, Property tituloEspanol,
                                          Property genero, Property duracion, Property clasificacion,
                                          Property estreno, Property estado, Property preventa, Property formato,
                                          String title, String spanishTitle, String genre, int duration,
                                          String rating, String releaseDate, String status, boolean preSale, String format) {

        // Construimos la URI del recurso de forma determinista para el ejemplo
        Resource movie = model.createResource("http://example.org/pelicula/" + id);

        // Propiedades básicas de texto
        movie.addProperty(titulo, title);
        movie.addProperty(tituloEspanol, spanishTitle);

        // Género: permitimos una cadena con múltiples géneros separados por coma
        if (genre.contains(",")) {
            String[] genres = genre.split(", ");
            for (String g : genres) {
                movie.addProperty(genero, g);
            }
        } else {
            movie.addProperty(genero, genre);
        }

        // Duración: añadimos como literal numérico sólo si es mayor a 0
        if (duration > 0) {
            movie.addLiteral(duracion, duration);
        }

        // Clasificación, fecha tipada (xsd:date), estado y flags
        movie.addProperty(clasificacion, rating);
        movie.addLiteral(estreno, model.createTypedLiteral(releaseDate, XSDDatatype.XSDdate));
        movie.addProperty(estado, status);
        movie.addLiteral(preventa, preSale);
        movie.addProperty(formato, format);
    }

    /**
     * Persiste en disco el modelo RDF en formato RDF/XML abreviado.
     * El método utiliza try-with-resources para asegurar el cierre del stream.
     *
     * @param model Modelo RDF a escribir en disco.
     */
    private static void saveRDFModel(Model model) {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE)) {
            model.write(out, "RDF/XML-ABBREV");
            System.out.println("✓ Base de datos RDF guardada en: " + RDF_FILE);
            System.out.println("✓ Total de triples creados: " + model.size() + "\n");
        } catch (IOException e) {
            System.err.println("Error guardando archivo RDF: " + e.getMessage());
        }
    }

    /**
     * Carga el modelo RDF desde el archivo en disco. Se utiliza FileManager de
     * Jena para buscar y abrir el recurso. Retorna null en caso de error.
     *
     * @return Modelo RDF cargado o null si ocurre un error.
     */
    private static Model loadRDFModel() {
        try {
            Model model = ModelFactory.createDefaultModel();
            InputStream in = FileManager.get().open(RDF_FILE);
            if (in == null) {
                throw new IllegalArgumentException("Archivo no encontrado: " + RDF_FILE);
            }
            model.read(in, null, "RDF/XML");
            in.close();
            System.out.println("✓ Base de datos RDF cargada exitosamente");
            return model;
        } catch (Exception e) {
            System.err.println("Error cargando el archivo RDF: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ejecuta un conjunto de consultas SPARQL de ejemplo sobre el modelo
     * proporcionado y muestra los resultados en tablas ASCII claro/compactas.
     *
     * Las consultas están diseñadas para ilustrar distintas operaciones: filtros,
     * ordenamientos, agregaciones (AVG, COUNT) y uso de tipos xsd:date.
     */
    private static void executeDemoQueries(Model model) {
        System.out.println("=== EJECUCIÓN DE CONSULTAS SPARQL ===\n");
        String[] queries = new String[] {
            // 1. Todas las películas en cartelera con sus fechas de estreno
            "PREFIX cine: <" + NS + ">\n" +
            "SELECT ?tituloEspanol ?estreno ?duracion WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:estreno ?estreno .\n" +
            "    ?pelicula cine:duracion ?duracion .\n" +
            "    ?pelicula cine:estado \"En cartelera\" .\n" +
            "} ORDER BY ?estreno",

            // 2. Películas disponibles en preventa
            "PREFIX cine: <" + NS + ">\n" +
            "SELECT ?tituloEspanol ?estreno ?formato WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:preventa true .\n" +
            "    ?pelicula cine:estreno ?estreno .\n" +
            "    ?pelicula cine:formato ?formato .\n" +
            "} ORDER BY ?estreno",

            // 3. Películas de más de 2 horas
            "PREFIX cine: <" + NS + ">\n" +
            "SELECT ?tituloEspanol ?duracion ?genero WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:duracion ?duracion .\n" +
            "    ?pelicula cine:genero ?genero .\n" +
            "    FILTER (?duracion > 120)\n" +
            "} ORDER BY DESC(?duracion)",

            // 4. Películas por clasificación de edad
            "PREFIX cine: <" + NS + ">\n" +
            "SELECT ?tituloEspanol ?clasificacion ?genero WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:clasificacion ?clasificacion .\n" +
            "    ?pelicula cine:genero ?genero .\n" +
            "} ORDER BY ?clasificacion",

            // 5. Conciertos y eventos especiales
            "PREFIX cine: <" + NS + ">\n" +
            "SELECT ?tituloEspanol ?duracion ?estreno WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:genero ?genero .\n" +
            "    ?pelicula cine:duracion ?duracion .\n" +
            "    ?pelicula cine:estreno ?estreno .\n" +
            "    FILTER (?genero = \"Concierto\" || ?genero = \"Documental\")\n" +
            "}",

            // 6. Películas estrenadas en noviembre 2025
            "PREFIX cine: <" + NS + ">\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT ?tituloEspanol ?estreno ?genero WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:estreno ?estreno .\n" +
            "    ?pelicula cine:genero ?genero .\n" +
            "    FILTER (?estreno >= \"2025-11-01\"^^xsd:date && ?estreno <= \"2025-11-30\"^^xsd:date)\n" +
            "} ORDER BY ?estreno",

            // 7. Películas familiares y animadas
            "PREFIX cine: <" + NS + ">\n" +
            "SELECT ?tituloEspanol ?clasificacion ?duracion WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:genero ?genero .\n" +
            "    ?pelicula cine:clasificacion ?clasificacion .\n" +
            "    ?pelicula cine:duracion ?duracion .\n" +
            "    FILTER (?genero = \"Familiar\" || ?genero = \"Animación\")\n" +
            "}",

            // 8. Duración promedio de las películas por género
            "PREFIX cine: <" + NS + ">\n" +
            "SELECT ?genero (AVG(?duracion) AS ?duracionPromedio) (COUNT(?pelicula) AS ?totalPeliculas) WHERE {\n" +
            "    ?pelicula cine:genero ?genero .\n" +
            "    ?pelicula cine:duracion ?duracion .\n" +
            "    FILTER (?duracion > 0)\n" +
            "} GROUP BY ?genero ORDER BY DESC(?duracionPromedio)",

            // 9. Películas de terror y suspenso
            "PREFIX cine: <" + NS + ">\n" +
            "SELECT ?tituloEspanol ?clasificacion ?duracion WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:genero ?genero .\n" +
            "    ?pelicula cine:clasificacion ?clasificacion .\n" +
            "    ?pelicula cine:duracion ?duracion .\n" +
            "    FILTER (?genero = \"Terror\" || ?genero = \"Suspenso\" || ?genero = \"Thriller\")\n" +
            "}",

            // 10. Próximos estrenos (después de hoy)
            "PREFIX cine: <" + NS + ">\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT ?tituloEspanol ?estreno ?formato WHERE {\n" +
            "    ?pelicula cine:tituloEspanol ?tituloEspanol .\n" +
            "    ?pelicula cine:estreno ?estreno .\n" +
            "    ?pelicula cine:formato ?formato .\n" +
            "    FILTER (?estreno > \"2025-11-13\"^^xsd:date)\n" +
            "} ORDER BY ?estreno"
        };

        String[] titles = new String[] {
            "1. PELÍCULAS EN CARTELERA",
            "2. PREVENTAS",
            "3. >2 HORAS",
            "4. POR CLASIFICACIÓN",
            "5. CONCIERTOS Y EVENTOS",
            "6. ESTRENOS NOV 2025",
            "7. FAMILIA/ANIMACIÓN",
            "8. DURACIÓN PROMEDIO POR GÉNERO",
            "9. TERROR / SUSPENSO",
            "10. PRÓXIMOS ESTRENOS"
        };

        for (int i = 0; i < queries.length; i++) {
            System.out.println("--- " + titles[i] + " ---");
            executeSPARQLQuery(model, queries[i]);
            System.out.println();
        }
    }
    
    private static void executeSPARQLQuery(Model model, String queryStr) {
        try {
            Query query = QueryFactory.create(queryStr);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                ResultSet results = qexec.execSelect();
                List<QuerySolution> rows = ResultSetFormatter.toList(results);
                List<String> vars = query.getResultVars();

                if (rows.isEmpty()) {
                    System.out.println("(sin resultados)");
                    return;
                }

                int cols = vars.size();
                final int MAX_COL_WIDTH = 40; // max width for any displayed column
                int[] widths = new int[cols];
                for (int i = 0; i < cols; i++) {
                    widths[i] = Math.min(vars.get(i).length(), MAX_COL_WIDTH);
                }

                // create a compact display table by truncating long values
                List<List<String>> displayTable = new ArrayList<>();
                for (QuerySolution qs : rows) {
                    List<String> displayRow = new ArrayList<>();
                    for (int i = 0; i < cols; i++) {
                        String v = vars.get(i);
                        String val = "";
                        if (qs.contains(v)) {
                            RDFNode node = qs.get(v);
                            if (node.isLiteral()) val = node.asLiteral().getString();
                            else val = node.toString();
                        }
                        String display = val;
                        if (display.length() > MAX_COL_WIDTH) display = display.substring(0, MAX_COL_WIDTH - 3) + "...";
                        displayRow.add(display);
                        widths[i] = Math.max(widths[i], display.length());
                    }
                    displayTable.add(displayRow);
                }

                // build separators
                StringBuilder sep = new StringBuilder();
                sep.append("+");
                for (int w : widths) {
                    sep.append(repeat('-', w + 2));
                    sep.append("+");
                }

                // header (use nicer labels and center)
                StringBuilder header = new StringBuilder();
                header.append("|");
                for (int i = 0; i < cols; i++) {
                    String label = niceLabel(vars.get(i));
                    header.append(' ').append(center(label, widths[i])).append(' ').append('|');
                }

                System.out.println(sep.toString());
                System.out.println(header.toString());
                System.out.println(sep.toString());

                // print each row as single line
                for (List<String> row : displayTable) {
                    StringBuilder rb = new StringBuilder();
                    rb.append('|');
                    for (int i = 0; i < cols; i++) {
                        String cell = row.get(i);
                        boolean rightAlign = isNumeric(cell);
                        rb.append(' ').append(formatCell(cell, widths[i], rightAlign)).append(' ').append('|');
                    }
                    System.out.println(rb.toString());
                }

                // final table separator
                System.out.println(sep.toString());
            }
        } catch (Exception e) {
            System.err.println("Error en consulta SPARQL: " + e.getMessage());
        }
    }

    private static String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        return s + "".repeat(width - s.length());
    }

    private static String repeat(char c, int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }

    private static String niceLabel(String var) {
        if (var == null || var.isEmpty()) return "";
        // common mappings
        Map<String,String> map = new HashMap<>();
        map.put("tituloEspanol", "tituloEspanol");
        map.put("titulo", "titulo");
        map.put("estreno", "estreno");
        map.put("duracion", "duracion");
        map.put("genero", "genero");
        map.put("clasificacion", "clasificacion");
        map.put("preventa", "preventa");
        map.put("formato", "formato");
        map.put("duracionPromedio", "duracionPromedio");
        map.put("totalPeliculas", "totalPeliculas");
        // default: transform camelCase/underscores to readable label
        if (map.containsKey(var)) return map.get(var);
        String t = var.replaceAll("([a-z])([A-Z])","$1 $2").replace('_', ' ');
        return t;
    }

    private static String formatCell(String s, int width, boolean rightAlign) {
        if (s == null) s = "";
        if (s.length() > width) s = s.substring(0, width);
        int pad = width - s.length();
        if (pad <= 0) return s;
        if (rightAlign) {
            return "".repeat(pad) + s;
        } else {
            return s + "".repeat(pad);
        }
    }

    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.matches("^-?\\d+(\\.\\d+)?$");
    }

    private static List<String> wrapToLines(String s, int maxWidth) {
        List<String> out = new ArrayList<>();
        if (s == null) {
            out.add("");
            return out;
        }
        String remaining = s;
        while (!remaining.isEmpty()) {
            if (remaining.length() <= maxWidth) {
                out.add(remaining);
                break;
            }
            // try to break at last space within maxWidth
            int breakPos = remaining.lastIndexOf(' ', maxWidth);
            if (breakPos <= 0) breakPos = maxWidth; // hard break
            out.add(remaining.substring(0, breakPos));
            remaining = remaining.substring(breakPos).trim();
        }
        if (out.isEmpty()) out.add("");
        return out;
    }

    private static String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        int totalPad = width - s.length();
        int left = totalPad / 2;
        int right = totalPad - left;
        return "".repeat(left) + s + "".repeat(right);
    }
}

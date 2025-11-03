package servicios;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import entidades.RegistroTemperatura;

public class TemperaturaServicio {
    public static List<RegistroTemperatura> getDatos(String nombreArchivo) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            Stream<String> lineas = Files.lines(Paths.get(nombreArchivo));

            return lineas.skip(1)
                    .map(linea -> linea.split(","))
                    .map(textos -> new RegistroTemperatura(
                            textos[0],
                            LocalDate.parse(textos[1], formatoFecha),
                            Double.parseDouble(textos[2])
                    ))
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            System.out.println(ex);
            return Collections.emptyList();
        }
    }

    public static List<String> getCiudades(List<RegistroTemperatura> registros) {
        return registros.stream()
                .map(RegistroTemperatura::getCiudad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Filtrar por ciudad y rango de fechas
    public static List<RegistroTemperatura> filtrar(List<RegistroTemperatura> registros,
            String ciudad, LocalDate desde, LocalDate hasta) {
        return registros.stream()
                .filter(r -> r.getCiudad().equals(ciudad) &&
                        !(r.getFecha().isAfter(hasta) || r.getFecha().isBefore(desde)))
                .collect(Collectors.toList());
    }

    // Extraer temperaturas (para gráficas o cálculos)
    public static List<Double> extraerTemperaturas(List<RegistroTemperatura> registros) {
        return registros.stream()
                .map(RegistroTemperatura::getTemperatura)
                .collect(Collectors.toList());
    }

    // Calcular promedio de una ciudad especifica
    public static double getPromedio(List<Double> datos) {
        return datos.isEmpty() ? 0 : datos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    // Para calcular máximo
    public static double getMaximo(List<Double> datos) {
        return datos.isEmpty() ? 0 : datos.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    // Para calcular mínimo
    public static double getMinimo(List<Double> datos) {
        return datos.isEmpty() ? 0 : datos.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    // Para Usar en las estadísticas 
    public static Map<String, Double> getEstadisticas(List<RegistroTemperatura> registros,
            String ciudad, LocalDate desde, LocalDate hasta) {

        var registrosFiltrados = filtrar(registros, ciudad, desde, hasta);
        var temperaturas = extraerTemperaturas(registrosFiltrados);

        Map<String, Double> estadisticas = new LinkedHashMap<>();
        estadisticas.put("Promedio", getPromedio(temperaturas));
        estadisticas.put("Máximo", getMaximo(temperaturas));
        estadisticas.put("Mínimo", getMinimo(temperaturas));

        return estadisticas;
    }

    // Para Calcular promedios de todas las ciudades en un rango de fechas
    public static Map<String, Double> promedioPorCiudad(List<RegistroTemperatura> registros,
            LocalDate desde, LocalDate hasta) {

        List<String> ciudades = getCiudades(registros);

        return ciudades.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        ciudad -> getPromedio(
                                extraerTemperaturas(filtrar(registros, ciudad, desde, hasta))
                        )
                ));
    }
}

package cl.duocuc.sanossalvos.matchingengine.service;

import cl.duocuc.sanossalvos.matchingengine.dto.ext.ReporteDto;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calcula la puntuación de similitud entre dos reportes (0–100).
 *
 * Criterios:
 *  - Especie        → obligatorio; si difiere la puntuación es 0
 *  - Colores        → hasta 40 puntos (proporción de colores en común)
 *  - Distancia (km) → hasta 40 puntos (<2→40, <5→30, <10→20, <20→10, ≥20→0)
 *  - Género         → 20 puntos si coincide y ninguno es DESCONOCIDO
 */
@Service
public class PuntuacionService {

    public static final int PUNTUACION_MINIMA = 30;

    public int calcular(ReporteDto perdido, ReporteDto encontrado, double distanciaKm) {

        // Especie: condición obligatoria
        if (perdido.getEspecie() == null || !perdido.getEspecie().equalsIgnoreCase(encontrado.getEspecie())) {
            return 0;
        }

        int puntos = 0;

        // Colores (máx 40 pts)
        puntos += puntosColores(perdido.getColores(), encontrado.getColores());

        // Distancia (máx 40 pts)
        puntos += puntosDistancia(distanciaKm);

        // Género (20 pts)
        puntos += puntosGenero(perdido.getGenero(), encontrado.getGenero());

        return puntos;
    }

    int puntosColores(List<String> coloresA, List<String> coloresB) {
        if (coloresA == null || coloresA.isEmpty() || coloresB == null || coloresB.isEmpty()) {
            return 0;
        }
        Set<String> setA = new HashSet<>(coloresA.stream().map(String::toLowerCase).toList());
        Set<String> setB = new HashSet<>(coloresB.stream().map(String::toLowerCase).toList());

        Set<String> interseccion = new HashSet<>(setA);
        interseccion.retainAll(setB);

        int maximo = Math.max(setA.size(), setB.size());
        return (int) Math.round((double) interseccion.size() / maximo * 40);
    }

    int puntosDistancia(double distanciaKm) {
        if (distanciaKm < 2)  return 40;
        if (distanciaKm < 5)  return 30;
        if (distanciaKm < 10) return 20;
        if (distanciaKm < 20) return 10;
        return 0;
    }

    int puntosGenero(String generoA, String generoB) {
        if (generoA == null || generoB == null) return 0;
        if ("DESCONOCIDO".equalsIgnoreCase(generoA) || "DESCONOCIDO".equalsIgnoreCase(generoB)) return 0;
        return generoA.equalsIgnoreCase(generoB) ? 20 : 0;
    }
}

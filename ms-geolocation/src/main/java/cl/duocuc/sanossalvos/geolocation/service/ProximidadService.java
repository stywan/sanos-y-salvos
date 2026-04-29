package cl.duocuc.sanossalvos.geolocation.service;

import cl.duocuc.sanossalvos.geolocation.dto.FiltrarCercanosRequest;
import cl.duocuc.sanossalvos.geolocation.dto.PuntoCercanoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProximidadService {

    private static final double RADIO_TIERRA_KM = 6371.0;

    /**
     * Filtra una lista de puntos y devuelve únicamente los que caen dentro del radio dado.
     * Ordena el resultado de más cercano a más lejano.
     */
    public List<PuntoCercanoDto> filtrarCercanos(FiltrarCercanosRequest request) {
        double latCentro = request.getLatitud();
        double lonCentro = request.getLongitud();
        double radio     = request.getRadioKm();

        return request.getPuntos().stream()
                .map(p -> {
                    double dist = calcularDistanciaKm(latCentro, lonCentro, p.getLatitud(), p.getLongitud());
                    return PuntoCercanoDto.builder()
                            .id(p.getId())
                            .latitud(p.getLatitud())
                            .longitud(p.getLongitud())
                            .distanciaKm(Math.round(dist * 100.0) / 100.0)
                            .build();
                })
                .filter(p -> p.getDistanciaKm() <= radio)
                .sorted((a, b) -> Double.compare(a.getDistanciaKm(), b.getDistanciaKm()))
                .toList();
    }

    /**
     * Fórmula de Haversine: distancia en km entre dos coordenadas geográficas.
     */
    public double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }
}

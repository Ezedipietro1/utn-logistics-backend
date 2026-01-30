package com.SolicitudTraslado.services;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class OsrmService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(OsrmService.class);

    @Value("${osrm.base-url:http://osrm:5000}")
    private String osrmBaseUrl;

    /**
     * Consulta OSRM route/v1/driving y devuelve distancia en metros y duración en segundos.
     * Retorna un Map con claves: "distanceMeters" (Double), "durationSeconds" (Double), "units" ("m","s").
     */
    public Map<String, Object> getDistanceDuration(double origenLat, double origenLng, double destinoLat,
            double destinoLng) {

        Map<String, Object> result = new HashMap<>();

        try {
            // OSRM espera lon,lat
            String coords = String.format("%s,%s;%s,%s", origenLng, origenLat, destinoLng, destinoLat);

            URI uri = UriComponentsBuilder.fromHttpUrl(osrmBaseUrl)
                    .pathSegment("route", "v1", "driving", coords)
                    .queryParam("overview", "false")
                    .queryParam("alternatives", "false")
                    .queryParam("steps", "false")
                    .build(true).toUri();

            String resp = restTemplate.getForObject(uri, String.class);
            if (resp == null) {
                result.put("error", "Empty response from OSRM");
                return result;
            }

            JsonNode root = mapper.readTree(resp);
            if (root.has("routes") && root.get("routes").isArray() && root.get("routes").size() > 0) {
                JsonNode route = root.get("routes").get(0);
                double distance = route.has("distance") ? route.get("distance").asDouble() : Double.NaN;
                double duration = route.has("duration") ? route.get("duration").asDouble() : Double.NaN;

                result.put("distanceMeters", distance);
                result.put("durationSeconds", duration);
                result.put("units", "m,s");
                result.put("osrm_code", root.has("code") ? root.get("code").asText() : "");
                return result;
            } else if (root.has("message")) {
                result.put("error", root.get("message").asText());
                return result;
            } else {
                result.put("error", "No route returned by OSRM");
                return result;
            }

        } catch (RestClientException e) {
            log.error("Error calling OSRM", e);
            result.put("error", "Error calling OSRM: " + e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("Error parsing OSRM response", e);
            result.put("error", "Error parsing OSRM response: " + e.getMessage());
            return result;
        }
    }

}

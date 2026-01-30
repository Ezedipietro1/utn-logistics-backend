package com.SolicitudTraslado.controller;

import java.util.Map;

import com.SolicitudTraslado.services.OsrmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitudes/osrm")
public class OsrmController {

    private final OsrmService osrmService;

    public OsrmController(OsrmService osrmService) {
        this.osrmService = osrmService;
    }

    @GetMapping("/route")
    public ResponseEntity<?> route(@RequestParam("oLat") double oLat, @RequestParam("oLng") double oLng,
            @RequestParam("dLat") double dLat, @RequestParam("dLng") double dLng) {

        Map<String, Object> resp = osrmService.getDistanceDuration(oLat, oLng, dLat, dLng);
        if (resp.containsKey("error")) {
            return ResponseEntity.badRequest().body(resp);
        }
        return ResponseEntity.ok(resp);
    }

}

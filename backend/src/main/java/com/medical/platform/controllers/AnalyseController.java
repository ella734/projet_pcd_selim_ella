package com.medical.platform.controllers;

import com.medical.platform.entities.Transplantation;
import com.medical.platform.graph.AnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ia")
@PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN')")
public class AnalyseController {

    @Autowired private AnalyseService analyseService;

    @PostMapping("/analyser")
    public ResponseEntity<Map<String, Object>> analyser(@RequestBody Transplantation t) {
        return ResponseEntity.ok(analyseService.analyser(t));
    }
}

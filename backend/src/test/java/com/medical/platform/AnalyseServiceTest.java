package com.medical.platform;

import com.medical.platform.entities.Transplantation;
import com.medical.platform.graph.AnalyseService;
import com.medical.platform.service.AuthRateLimiterService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyseServiceTest {

    private final AnalyseService analyseService = new AnalyseService();

    @Test
    void risqueEleveQuandRetourDialyse() {
        Transplantation t = new Transplantation();
        t.setRetourDialyse(true);

        Map<String, Object> result = analyseService.analyser(t);

        assertEquals("ÉLEVÉ 🔴", result.get("niveauRisque"));
    }

    @Test
    void risqueFaibleSansFacteur() {
        Map<String, Object> result = analyseService.analyser(new Transplantation());

        assertEquals("FAIBLE 🟢", result.get("niveauRisque"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void alerteDfgCritique() {
        Transplantation t = new Transplantation();
        t.setDfgMdrdM3(25.0);

        List<String> alertes = (List<String>) analyseService.analyser(t).get("alertes");

        assertTrue(alertes.stream().anyMatch(a -> a.contains("DFG critique")));
    }

    @Test
    void rateLimitBloqueApres5Tentatives() {
        AuthRateLimiterService limiter = new AuthRateLimiterService(5, 900);
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("testuser|127.0.0.1");
        }

        assertTrue(limiter.isBlocked("testuser|127.0.0.1"));
    }
}

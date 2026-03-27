package com.realestate.freetextsearch.controller;

import com.realestate.freetextsearch.service.NlpParserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ParseController {

    private final NlpParserService parserService;

    public ParseController(NlpParserService parserService) {
        this.parserService = parserService;
    }

    /**
     * POST /api/parse
     * Body: { "query": "3bhk flat in bandra under 1 crore", "geoOverride": null }
     */
    @PostMapping("/parse")
    public Map<String, Object> parse(@RequestBody Map<String, Object> request) {
        String query = (String) request.getOrDefault("query", "");
        Integer geoOverride = request.get("geoOverride") != null
                ? ((Number) request.get("geoOverride")).intValue()
                : null;
        return parserService.parseQuery(query, geoOverride);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "Real Estate Free Text Search API");
    }
}

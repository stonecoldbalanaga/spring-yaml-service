package com.example.yamlservice.controller;

import com.example.yamlservice.model.AppValues;
import com.example.yamlservice.service.AppValuesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for accessing AppValues from the YAML file.
 *
 * All endpoints require a valid JWT — see AuthController to obtain one.
 *
 * GET  /api/values        — return all key-value pairs
 * GET  /api/values/{key}  — return a single value by key
 * GET  /api/values/reload — force re-read of the YAML file (ADMIN only)
 */
@RestController
@RequestMapping("/api/values")
public class AppValuesController {

    private final AppValuesService appValuesService;

    public AppValuesController(AppValuesService appValuesService) {
        this.appValuesService = appValuesService;
    }

    /**
     * Return all key-value pairs from AppValues.
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> getAllValues() {
        AppValues appValues = appValuesService.loadAndValidate();
        return ResponseEntity.ok(appValues.getEntries());
    }

    /**
     * Return a single value by key.
     *
     * @param key the AppValues key to look up
     */
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> getValueByKey(@PathVariable String key) {
        String value = appValuesService.getValue(key);
        return ResponseEntity.ok(Map.of(key, value));
    }

    /**
     * Force a reload of the YAML file. Restricted to ADMIN role.
     * Because AppValuesService reads the file on every call, this endpoint
     * simply triggers a validation pass and reports the entry count.
     */
    @GetMapping("/reload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reload() {
        AppValues appValues = appValuesService.loadAndValidate();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "entriesLoaded", appValues.getEntries().size()
        ));
    }
}

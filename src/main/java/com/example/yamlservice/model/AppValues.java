package com.example.yamlservice.model;

import java.util.Map;

/**
 * Represents the validated structure of the YAML file.
 * Root element must be "AppValues" containing a map of key-value pairs.
 */
public class AppValues {

    private Map<String, String> entries;

    public AppValues() {}

    public AppValues(Map<String, String> entries) {
        this.entries = entries;
    }

    public Map<String, String> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, String> entries) {
        this.entries = entries;
    }
}

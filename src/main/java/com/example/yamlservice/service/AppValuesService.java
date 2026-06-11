package com.example.yamlservice.service;

import com.example.yamlservice.exception.YamlFileNotFoundException;
import com.example.yamlservice.exception.YamlValidationException;
import com.example.yamlservice.model.AppValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads, validates, and exposes the AppValues YAML file.
 *
 * <p>Expected YAML structure:
 * <pre>
 * AppValues:
 *   key1: value1
 *   key2: value2
 * </pre>
 */
@Service
public class AppValuesService {

    private static final Logger log = LoggerFactory.getLogger(AppValuesService.class);
    private static final String ROOT_KEY = "AppValues";

    @Value("${app.yaml.file-path:appvalues.yaml}")
    private String yamlFilePath;

    /**
     * Load, validate and return the AppValues map.
     *
     * @return AppValues containing a validated key-value map
     */
    public AppValues loadAndValidate() {
        Map<String, Object> rawDocument = parseYaml();
        return validate(rawDocument);
    }

    /**
     * Return a single value by key.
     *
     * @param key the key to look up
     * @return the string value
     * @throws YamlValidationException if the key does not exist
     */
    public String getValue(String key) {
        AppValues appValues = loadAndValidate();
        String value = appValues.getEntries().get(key);
        if (value == null) {
            throw new YamlValidationException(
                    "Key '" + key + "' not found in AppValues");
        }
        return value;
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> parseYaml() {
        Yaml yaml = new Yaml();

        // 1. Try classpath first (embedded resource)
        InputStream classpathStream =
                getClass().getClassLoader().getResourceAsStream(yamlFilePath);
        if (classpathStream != null) {
            log.debug("Loading YAML from classpath: {}", yamlFilePath);
            try {
                return parseStream(yaml, classpathStream);
            } finally {
                try { classpathStream.close(); } catch (IOException ignored) {}
            }
        }

        // 2. Fall back to file system path
        Path fsPath = Paths.get(yamlFilePath);
        if (Files.exists(fsPath)) {
            log.debug("Loading YAML from filesystem: {}", fsPath.toAbsolutePath());
            try (InputStream fsStream = Files.newInputStream(fsPath)) {
                return parseStream(yaml, fsStream);
            } catch (IOException ex) {
                throw new YamlFileNotFoundException(
                        "Could not read YAML file at: " + fsPath.toAbsolutePath(), ex);
            }
        }

        throw new YamlFileNotFoundException(
                "YAML file not found on classpath or filesystem: " + yamlFilePath);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseStream(Yaml yaml, InputStream stream) {
        Object parsed = yaml.load(stream);
        if (parsed == null) {
            throw new YamlValidationException("YAML file is empty");
        }
        if (!(parsed instanceof Map)) {
            throw new YamlValidationException(
                    "YAML root element must be a mapping, got: " + parsed.getClass().getSimpleName());
        }
        return (Map<String, Object>) parsed;
    }

    @SuppressWarnings("unchecked")
    private AppValues validate(Map<String, Object> document) {
        // Rule 1: root must contain exactly one key — "AppValues"
        if (!document.containsKey(ROOT_KEY)) {
            throw new YamlValidationException(
                    "YAML root element must be '" + ROOT_KEY + "'. Found keys: " + document.keySet());
        }

        if (document.size() > 1) {
            throw new YamlValidationException(
                    "YAML document must have exactly one root key ('" + ROOT_KEY +
                    "'). Additional keys found: " + document.keySet());
        }

        // Rule 2: AppValues must be a mapping, not null
        Object appValuesRaw = document.get(ROOT_KEY);
        if (appValuesRaw == null) {
            throw new YamlValidationException(
                    "'" + ROOT_KEY + "' must contain key-value pairs, but it is null or empty");
        }
        if (!(appValuesRaw instanceof Map)) {
            throw new YamlValidationException(
                    "'" + ROOT_KEY + "' must be a mapping of key-value pairs, got: " +
                    appValuesRaw.getClass().getSimpleName());
        }

        Map<?, ?> rawEntries = (Map<?, ?>) appValuesRaw;

        // Rule 3: must have at least one entry
        if (rawEntries.isEmpty()) {
            throw new YamlValidationException(
                    "'" + ROOT_KEY + "' must contain at least one key-value pair");
        }

        // Rule 4: all keys and values must be non-null strings
        Map<String, String> entries = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawEntries.entrySet()) {
            Object k = entry.getKey();
            Object v = entry.getValue();

            if (k == null) {
                throw new YamlValidationException("AppValues contains a null key");
            }
            if (!(k instanceof String)) {
                throw new YamlValidationException(
                        "All keys in AppValues must be strings, found: " + k.getClass().getSimpleName());
            }
            if (v == null) {
                throw new YamlValidationException(
                        "Value for key '" + k + "' must not be null");
            }
            if (v instanceof Map || v instanceof Iterable) {
                throw new YamlValidationException(
                        "AppValues must contain only scalar key-value pairs. " +
                        "Key '" + k + "' maps to a nested structure");
            }

            entries.put((String) k, String.valueOf(v));
        }

        log.debug("YAML validated successfully: {} entries loaded", entries.size());
        return new AppValues(entries);
    }
}

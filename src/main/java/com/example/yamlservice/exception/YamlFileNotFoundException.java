package com.example.yamlservice.exception;

/**
 * Thrown when the configured YAML file cannot be found or read.
 */
public class YamlFileNotFoundException extends RuntimeException {

    public YamlFileNotFoundException(String message) {
        super(message);
    }

    public YamlFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

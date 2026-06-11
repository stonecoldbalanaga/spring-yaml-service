package com.example.yamlservice.exception;

/**
 * Thrown when the YAML file fails structural or content validation.
 */
public class YamlValidationException extends RuntimeException {

    public YamlValidationException(String message) {
        super(message);
    }

    public YamlValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

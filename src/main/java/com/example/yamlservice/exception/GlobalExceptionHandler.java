package com.example.yamlservice.exception;

import com.example.yamlservice.model.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralised exception → HTTP response mapping.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(YamlValidationException.class)
    public ResponseEntity<ApiError> handleYamlValidation(
            YamlValidationException ex, HttpServletRequest req) {
        ApiError error = new ApiError(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "YAML Validation Error",
                ex.getMessage(),
                req.getRequestURI());
        return ResponseEntity.unprocessableEntity().body(error);
    }

    @ExceptionHandler(YamlFileNotFoundException.class)
    public ResponseEntity<ApiError> handleYamlFileNotFound(
            YamlFileNotFoundException ex, HttpServletRequest req) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "YAML File Not Found",
                ex.getMessage(),
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid username or password",
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request body validation failed",
                req.getRequestURI());
        error.setDetails(details);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {
        List<String> details = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.toList());
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Constraint Violation",
                "Request parameter validation failed",
                req.getRequestURI());
        error.setDetails(details);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex, HttpServletRequest req) {
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                req.getRequestURI());
        return ResponseEntity.internalServerError().body(error);
    }
}

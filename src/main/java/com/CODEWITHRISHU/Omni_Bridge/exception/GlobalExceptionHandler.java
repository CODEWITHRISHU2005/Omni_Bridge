package com.CODEWITHRISHU.Omni_Bridge.exception;

import com.CODEWITHRISHU.Omni_Bridge.dto.IncidentApiModels.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            IncidentNotFoundException.class,
            VenueNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(
            RuntimeException exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                exception.getMessage(),
                request,
                Map.of());
    }

    @ExceptionHandler(InvalidIncidentTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidTransition(
            InvalidIncidentTransitionException exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.CONFLICT,
                "Invalid Incident State",
                exception.getMessage(),
                request,
                Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            validationErrors.putIfAbsent(
                    fieldError.getField(), fieldError.getDefaultMessage());
        }

        return error(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more request fields are invalid.",
                request,
                validationErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            validationErrors.put(
                    violation.getPropertyPath().toString(), violation.getMessage());
        }

        return error(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more request values are invalid.",
                request,
                validationErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Malformed Request",
                "The JSON body is missing or invalid.",
                request,
                Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                exception.getMessage(),
                request,
                Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                request,
                Map.of());
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String title,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                title,
                message,
                request.getRequestURI(),
                validationErrors);
        return ResponseEntity.status(status).body(body);
    }
}

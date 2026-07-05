package com.nutritrack.exception;

import com.nutritrack.security.AccessGuard;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

/**
 * Catches AccessGuard.AccessDeniedException thrown anywhere in a controller and
 * converts it into a clean 403 response, so controllers don't need repetitive
 * try/catch blocks around every access check.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessGuard.AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessGuard.AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
    }
}

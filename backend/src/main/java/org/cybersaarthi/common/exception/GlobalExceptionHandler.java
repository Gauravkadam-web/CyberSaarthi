package org.cybersaarthi.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.cybersaarthi.common.response.ApiResponse;
import org.cybersaarthi.common.response.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex, HttpServletRequest request) {
        log.warn("API Exception [{}]: {}", ex.getStatus(), ex.getMessage());
        ApiResponse<Void> body = ApiResponse.error(
            ex.getMessage(),
            ex.getStatus().name(),
            ex.getStatus().value(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ValidationError> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()));
        }

        log.warn("Validation error on path {}: {} field errors", request.getRequestURI(), errors.size());

        ApiResponse<Void> body = ApiResponse.error(
            "Validation failed for request parameters",
            HttpStatus.BAD_REQUEST.name(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            errors
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(
            "Invalid email or password",
            HttpStatus.UNAUTHORIZED.name(),
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(
            "Full authentication is required to access this resource",
            HttpStatus.UNAUTHORIZED.name(),
            HttpStatus.UNAUTHORIZED.value(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(
            "Access denied: You do not have permission to access this resource",
            HttpStatus.FORBIDDEN.name(),
            HttpStatus.FORBIDDEN.value(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled internal server error on path {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ApiResponse<Void> body = ApiResponse.error(
            "An unexpected error occurred. Please contact system support.",
            HttpStatus.INTERNAL_SERVER_ERROR.name(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

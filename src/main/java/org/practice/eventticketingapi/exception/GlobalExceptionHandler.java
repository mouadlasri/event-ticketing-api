package org.practice.eventticketingapi.exception;

import org.practice.eventticketingapi.auth.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .toList();

        ErrorResponse errorResponse = new ErrorResponse("Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> domainNotFound(ResourceNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> resourceAlreadyExists(ResourceAlreadyExistsException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> invalidCredentials(InvalidCredentialsException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> forbiddenException(ForbiddenException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), null);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(InvalidResourceStateException.class)
    public ResponseEntity<ErrorResponse> invalidResourceStateException(InvalidResourceStateException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), null);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}

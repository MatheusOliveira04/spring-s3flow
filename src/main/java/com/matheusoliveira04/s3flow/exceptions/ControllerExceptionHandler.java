package com.matheusoliveira04.s3flow.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    ResponseEntity<StandardError> getFileNotFoundException(FileNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new StandardError(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI(), List.of(exception.getMessage()))
                );
    }

}

package com.aditya.File.share.spring.exceptions;

import org.springframework.dao.DuplicateKeyException;   // ✅ FIXED
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice   // ✅ FIXED
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleException(DuplicateKeyException e) {

        Map<String, Object> data = new HashMap<>();
        data.put("status", HttpStatus.CONFLICT.value());
        data.put("message", "Email already exists");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(data);
    }
}
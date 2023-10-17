package com.jordi.booknook.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {
                    errors.put(error.getField(), error.getDefaultMessage());
                });

        return errors;
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleConversionException(HttpMessageConversionException exception) {
        Map<String, String> errors = new HashMap<>();

        String errorMessage = String.valueOf(exception.getCause());
        String field = extractFieldValue(errorMessage);

        if (exception.getMessage().contains("Boolean")) {
            String message = "Please provide a valid Boolean (true or false).";
            errors.put(field, message);
        }

        if (exception.getMessage().contains("Long")){
            String message = "Please provide a valid id.";
            errors.put(field, message);
        }

        if (exception.getMessage().contains("Integer")) {
            String message = "Please provide a valid number.";
            errors.put(field, message);
        }

        return errors;
    }

    public static String extractFieldValue(String errorMessage) {
        Pattern pattern = Pattern.compile("\\[\"(\\w+)\"]");
        Matcher matcher = pattern.matcher(errorMessage);

       if (!matcher.find()){
        return "unknown_field";
       }

       return matcher.group(1);
    }
}
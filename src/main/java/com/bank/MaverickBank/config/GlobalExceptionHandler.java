package com.bank.MaverickBank.config;

import com.bank.MaverickBank.dto.response.ErrorDto;
import com.bank.MaverickBank.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    Logger log= LoggerFactory.getLogger("GlobalExceptionHandler.class");

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(
            ResourceNotFoundException e
    ){
        //always reply the object not the string
        log.warn("Entity not found in DB");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Entity not found in DB",
                                Instant.now())
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        Map<String, String> map = new HashMap<>();
        List<String> messages = new ArrayList<>();
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> list = bindingResult.getFieldErrors();
        list.forEach(fieldError -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
            messages.add(fieldError.getDefaultMessage());
            log.warn("Field {} , failed validation: {}", fieldError.getField(), fieldError.getDefaultMessage());
        });
        map.put("message", String.join(", ", messages));
        return ResponseEntity
                .badRequest()
                .body(map);
    }

    @ExceptionHandler(OutOfReachException.class)
    public ResponseEntity<ErrorDto> handleOutOfReachException(
            OutOfReachException e
    ){
        //always reply the object not the string
        log.warn("Entity is out of Reach for User");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Entity is out of Reach for User",
                                Instant.now())
                );
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleTypeMismatchException(
            TypeMismatchException e
    ){
        //always reply the object not the string
        log.warn("The given input is of wrong type");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "The given input is of wrong type",
                                Instant.now())
                );
    }


    @ExceptionHandler(IncompatibleJobException.class)
    public ResponseEntity<ErrorDto> handleIncompatibleJobException(
            IncompatibleJobException e
    ){
        //always reply the object not the string
        log.warn("User unrelated work");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "User unrelated work",
                                Instant.now())
                );
    }

    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<ErrorDto> handleOwnershipException(
            OwnershipException e
    ){
        //always reply the object not the string
        log.warn("Entity is not operable by User");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Entity is not operable by User",
                                Instant.now())
                );
    }

    @ExceptionHandler(LimitExceedException.class)
    public ResponseEntity<ErrorDto> handleLimitExceedException(
            LimitExceedException e
    ){
        //always reply the object not the string
        log.warn("The assigned limit has been exceeded");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "The assigned limit has been exceeded",
                                Instant.now())
                );
    }

    @ExceptionHandler(UnchangedStateException.class)
    public ResponseEntity<ErrorDto> handleUnchangedStateException(
            UnchangedStateException e
    ){
       log.warn("No valid change was made");
       return ResponseEntity
               .badRequest()
               .body(
                       new ErrorDto(e.getMessage(),
                               "No valid change was made",
                                    Instant.now())
               );
    }

    @ExceptionHandler(IncorrectPinException.class)
    public ResponseEntity<ErrorDto> handleIncorrectPinException(
            IncorrectPinException e
    ){
        log.warn("Authentication Failed");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Authentication Failed",
                                Instant.now())
                );
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ErrorDto> handleInactiveUserException(
            InactiveUserException e
    ){
        log.warn("The User has been disabled");
        //always reply the object not the string
        return ResponseEntity
                .status(HttpStatus.PRECONDITION_FAILED)
                .body(
                        new ErrorDto(e.getMessage(),
                                "Contact ADMIN admin@helpdesk.com",
                                Instant.now())
                );
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorDto> handleNullPointerException(
            NullPointerException e
    ){
        //always reply the object not the string
        log.warn("Entity cannot be null");
        return ResponseEntity
                .status(HttpStatus.PRECONDITION_FAILED)
                .body(
                        new ErrorDto(e.getMessage(),
                                "File is mandatory",
                                Instant.now())
                );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorDto> handleIOException(
            IOException e
    ){
        //always reply the object not the string
        log.warn("This is an IOException");
        return ResponseEntity
                .status(HttpStatus.PRECONDITION_FAILED)
                .body(
                        new ErrorDto(e.getMessage(),
                                "File is mandatory",
                                Instant.now())
                );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(
            Exception e
    ){
        //always reply the object not the string
        log.warn("Invalid Data passed");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Invalid Data passed",
                                Instant.now())
                );
    }

}

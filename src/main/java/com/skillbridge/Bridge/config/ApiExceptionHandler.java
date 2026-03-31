package com.skillbridge.Bridge.config;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            CannotCreateTransactionException.class,
            TransactionSystemException.class,
            DataAccessException.class
    })
    public ResponseEntity<Map<String, String>> databaseUnavailable(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message",
                        "Database unavailable. Is MySQL running? Check bridge-backend/.env (MYSQL_*), "
                                + "that mysql_schema.sql was applied to database \"bridge\", "
                                + "and remove any stale application-local.properties pointing at Postgres."
                ));
    }
}

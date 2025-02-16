package ru.nsu.fit.mihanizzm.litecrm.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.nsu.fit.mihanizzm.litecrm.exception.InvalidPaymentTypeException;
import ru.nsu.fit.mihanizzm.litecrm.exception.NoTransactionsInThisPeriodException;
import ru.nsu.fit.mihanizzm.litecrm.exception.NullSellerIdException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerHasNoTransactionsException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerNotFoundException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerValidationException;
import ru.nsu.fit.mihanizzm.litecrm.exception.TransactionNotFoundException;
import ru.nsu.fit.mihanizzm.litecrm.exception.TransactionValidationException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ClientExceptionHandler {
    @ExceptionHandler(SellerNotFoundException.class)
    public ResponseEntity<?> handleSellerNotFoundException(SellerNotFoundException e) {
        log.error("got SellerNotFoundException", e);
        return new ResponseEntity<>(
                String.format("SellerNotFoundException handled: couldn't find seller with id: %d", e.getId()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<?> handleSellerNotFoundException(TransactionNotFoundException e) {
        log.error("got TransactionNotFoundException", e);
        return new ResponseEntity<>(
                String.format("TransactionNotFoundException handled: couldn't find transaction with id: %d", e.getId()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("got IllegalArgumentException", e);
        return new ResponseEntity<>(
                String.format("IllegalArgumentException handled: '%s'. Bad request.", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NoTransactionsInThisPeriodException.class)
    public ResponseEntity<?> handleNoTransactionsInThisPeriodException(NoTransactionsInThisPeriodException e) {
        log.error(
                "got NoTransactionsInThisPeriodException for period: {} to {} with message: {}",
                e.getStart(),
                e.getEnd(),
                e.getMessage()
        );
        return new ResponseEntity<>(
                String.format(
                        "There were no transactions in the given period: %s to %s",
                        e.getStart().toLocalDate().toString(),
                        e.getEnd().toLocalDate().toString()
                ),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(SellerHasNoTransactionsException.class)
    public ResponseEntity<?> handleSellerHasNoTransactionsException(SellerHasNoTransactionsException e) {
        log.error(
                "got SellerHasNoTransactionsException for seller with id: {}", e.getSellerId());
        return new ResponseEntity<>(
                String.format(
                        "Couldn't find the best day for seller with id: %d because it has zero transactions",
                        e.getSellerId()
                ),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(TransactionValidationException.class)
    public ResponseEntity<?> handleTransactionValidationException(TransactionValidationException e) {
        log.error("got TransactionValidationException", e);
        String errorMessage = e.getViolations().stream()
                .map(v -> "Property: " + v.getPropertyPath() + ", message: " + v.getMessage())
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                String.format("Validation failed for transaction. Validation errors:\n%s", errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(SellerValidationException.class)
    public ResponseEntity<?> handleSellerValidationException(SellerValidationException e) {
        log.error("got SellerValidationException", e);
        String errorMessage = e.getViolations().stream()
                .map(v -> "Property: " + v.getPropertyPath() + ", message: " + v.getMessage())
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                String.format("Validation failed for seller. Validation errors:\n%s", errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidPaymentTypeException.class)
    public ResponseEntity<?> handleInvalidPaymentTypeException(InvalidPaymentTypeException e) {
        log.error("got InvalidPaymentTypeException", e);
        return new ResponseEntity<>(
                String.format(
                        "Got invalid payment type: %s. Available types are: 'CARD', 'CASH', 'TRANSFER'",
                        e.getPassedPaymentType()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NullSellerIdException.class)
    public ResponseEntity<?> handleNullSellerIdException(NullSellerIdException e) {
        log.error("got NullSellerIdException", e);
        return new ResponseEntity<>(
                "Seller ID must not be null when creating a new transaction",
                HttpStatus.BAD_REQUEST
        );
    }
}

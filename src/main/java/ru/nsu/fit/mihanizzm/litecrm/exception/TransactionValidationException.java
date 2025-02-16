package ru.nsu.fit.mihanizzm.litecrm.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.nsu.fit.mihanizzm.litecrm.models.Transaction;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class TransactionValidationException extends RuntimeException {
    private final Set<ConstraintViolation<Transaction>> violations;
}

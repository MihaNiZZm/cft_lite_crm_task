package ru.nsu.fit.mihanizzm.litecrm.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvalidPaymentTypeException extends RuntimeException {
    private final String passedPaymentType;
}

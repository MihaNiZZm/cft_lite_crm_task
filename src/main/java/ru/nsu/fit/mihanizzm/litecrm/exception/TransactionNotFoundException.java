package ru.nsu.fit.mihanizzm.litecrm.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TransactionNotFoundException extends RuntimeException {
    private final Integer id;
}

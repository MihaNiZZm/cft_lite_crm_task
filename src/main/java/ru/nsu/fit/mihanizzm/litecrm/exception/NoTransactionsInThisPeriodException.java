package ru.nsu.fit.mihanizzm.litecrm.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class NoTransactionsInThisPeriodException extends RuntimeException {
    private final LocalDateTime start;
    private final LocalDateTime end;
}

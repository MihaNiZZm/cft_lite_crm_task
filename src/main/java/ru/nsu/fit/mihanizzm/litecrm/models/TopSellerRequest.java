package ru.nsu.fit.mihanizzm.litecrm.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopSellerRequest {
    private PeriodType periodType;
    private LocalDateTime periodStartDate;
}

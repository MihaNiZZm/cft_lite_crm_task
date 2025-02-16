package ru.nsu.fit.mihanizzm.litecrm.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(schema = "app", name = "transaction")
@Audited
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @NotNull(message = "Seller ID must not be null")
    @ManyToOne
    @JoinColumn(name = "seller_id", referencedColumnName = "id", nullable = false)
    private Seller seller;

    @NotNull(message = "Amount must not be null")
    @Min(value = 0, message = "Amount must be greater or equal 0.0")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "transaction_date", nullable = false)
    @NotAudited
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }
}

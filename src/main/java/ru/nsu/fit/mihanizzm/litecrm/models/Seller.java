package ru.nsu.fit.mihanizzm.litecrm.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(schema = "app", name = "seller")
@Audited
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @NotNull(message = "Name must not be null")
    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 255, message = "Name must have length at least 2 and at most 255")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255, message = "Contact info must have length at most 255")
    @NotBlank(message = "Contact info must not be blank")
    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "registration_date", nullable = false)
    @NotAudited
    private LocalDateTime registrationDate;

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Transaction> transactions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setSeller(this);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setSeller(null);
    }
}

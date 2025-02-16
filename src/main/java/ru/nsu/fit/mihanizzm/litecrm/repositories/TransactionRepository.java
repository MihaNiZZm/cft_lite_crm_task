package ru.nsu.fit.mihanizzm.litecrm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT t.seller, SUM(t.amount) as totalAmount " +
            "FROM Transaction t " +
            "WHERE t.transactionDate >= :start AND t.transactionDate < :end " +
            "GROUP BY t.seller " +
            "ORDER BY totalAmount DESC")
    Optional<Object[]> findTopSellerByPeriod(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("SELECT t.seller " +
            "FROM Transaction t " +
            "WHERE t.transactionDate >= :start AND t.transactionDate < :end " +
            "GROUP BY t.seller " +
            "HAVING SUM(t.amount) < :threshold")
    List<Seller> findSellersWithTotalAmountLessThan(@Param("threshold") BigDecimal threshold,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    @Query(value = "SELECT CAST(transaction_date AS DATE) as day, COUNT(*) as cnt " +
            "FROM app.transaction " +
            "WHERE seller_id = :sellerId " +
            "GROUP BY day " +
            "ORDER BY cnt DESC " +
            "LIMIT 1", nativeQuery = true)
    Object findBestDayForSellerNative(@Param("sellerId") Integer sellerId);
}

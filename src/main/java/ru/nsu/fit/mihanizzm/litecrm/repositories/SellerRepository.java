package ru.nsu.fit.mihanizzm.litecrm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Integer> {

}

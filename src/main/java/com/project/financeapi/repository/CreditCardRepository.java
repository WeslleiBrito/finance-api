package com.project.financeapi.repository;

import com.project.financeapi.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    @Query("""
            SELECT c FROM CreditCard c
            WHERE c.id = :cardId
              AND c.createdBy.id = :userId
              AND c.status != "CANCELED"
            """)
    Optional<CreditCard> findByCreatedByAndId(
            @Param("userId") String userId,
            @Param("cardId") UUID cardId
    );

    @Query("""
            SELECT c FROM CreditCard c
            WHERE c.status != "CANCELED" AND c.createdBy.id = :userId
            """)
    List<CreditCard> findAllByCreatedBy_Id(@Param("userId") String userId);

}

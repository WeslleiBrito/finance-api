package com.project.financeapi.repository;

import com.project.financeapi.entity.base.CardBase;
import com.project.financeapi.entity.payment.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<CardBase, UUID> {

    @Query("""
    SELECT c FROM CardBase c
    WHERE TYPE(c) = CreditCard
      AND c.createdBy.id = :userId
""")
    List<CreditCard> findAllCreditCardByCreatedBy(UUID userId);

    @Query("""
    SELECT c FROM CardBase c
    WHERE TYPE(c) = CreditCard
      AND c.id = :id
      AND c.createdBy.id = :userId
""")
    Optional<CreditCard> findCreditCardByCreatedByAndId(UUID userId, UUID id);


}

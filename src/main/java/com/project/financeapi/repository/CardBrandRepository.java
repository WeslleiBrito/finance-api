package com.project.financeapi.repository;

import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.CardBrandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardBrandRepository extends JpaRepository<CardBrand, UUID> {

    @Query(
            """
                SELECT c FROM CardBrand c
                WHERE (c.createdBy.id IS NULL OR c.createdBy.id = :userId) AND c.id = :id
            """)
    Optional<CardBrand> findByCreatedByAndId(@Param("userId") UUID userId, @Param("id") UUID id);

    @Query(
            """
                SELECT c FROM CardBrand c
                WHERE  c.createdBy IS NULL OR c.createdBy.id = :userId
            """)
    List<CardBrand> findAllByCreatedBy(@Param("userId") UUID userId);

    @Query(
            """
                SELECT COUNT(c) > 0
                FROM CardBrand c
                WHERE LOWER(c.name) = LOWER(:name) AND (c.createdBy IS NULL OR c.createdBy = :user)
            """
    )
    boolean nameExitsByCreatedBy(
            @Param("user") User user,
            @Param("name") String name
    );

    @Query(
            """
                SELECT c FROM CardBrand c
                WHERE c.status = :cardBrandStatus AND (c.createdBy IS NULL OR c.createdBy = :user)
            """
    )
    List<CardBrand> findAllByUserCardBrandStatus(
            @Param("user") User user,
            @Param("bankStatus") CardBrandStatus cardBrandStatus);
}

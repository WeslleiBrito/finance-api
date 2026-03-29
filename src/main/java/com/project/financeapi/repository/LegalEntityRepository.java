package com.project.financeapi.repository;

import com.project.financeapi.entity.LegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LegalEntityRepository extends JpaRepository<LegalEntity, UUID> {

    @Query(
            """
                SELECT e FROM LegalEntity e
                WHERE e.createdBy.id = :userId AND e.cnpj = :cnpj
            """
    )
    Optional<LegalEntity> findByCreatedBy_IdAndCNPJ(
            @Param("userId") String userId,
            @Param("cnpj") String cnpj
    );

    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
        FROM LegalEntity e
        WHERE e.createdBy.id = :userId
          AND e.cnpj = :cnpj
    """)
    boolean existsByCreatedBy_IdAndCnpj(
            @Param("userId") String userId,
            @Param("cnpj") String cnpj
    );
}

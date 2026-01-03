package com.project.financeapi.repository;

import com.project.financeapi.entity.PhysicalPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PhysicalPersonRepository extends JpaRepository<PhysicalPerson, UUID> {
    @Query(
            """
                SELECT p FROM PhysicalPerson p
                WHERE p.createdBy.id = :userId AND p.cpf = :cpf
            """
    )
    Optional<PhysicalPerson> findByCreatedBy_IdAndCPF(
            @Param("userId") UUID userId,
            @Param("cpf") String cpf
    );


        @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM PhysicalPerson p
        WHERE p.createdBy.id = :userId
          AND p.cpf = :cpf
    """)
        boolean existsByCreatedBy_IdAndCpf(
                @Param("userId") UUID userId,
                @Param("cpf") String cpf
        );

}

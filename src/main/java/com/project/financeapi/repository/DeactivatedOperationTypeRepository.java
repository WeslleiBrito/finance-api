package com.project.financeapi.repository;

import com.project.financeapi.entity.DeactivatedOperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DeactivatedOperationTypeRepository extends JpaRepository<DeactivatedOperationType, UUID> {

    // Retorna apenas os IDs em um Set (alta performance)
    @Query("SELECT d.operationType.id FROM DeactivatedOperationType d WHERE d.user.id = :userId")
    Set<UUID> findDeactivatedTypeIdsByUserId(@Param("userId") String userId);

    // Consulta para o botão de Ativar/Desativar
    Optional<DeactivatedOperationType> findByUserIdAndOperationTypeId(String userId, UUID operationTypeId);
}
package com.project.financeapi.repository;

import com.project.financeapi.entity.DeactivatedOperationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DeactivatedOperationGroupRepository extends JpaRepository<DeactivatedOperationGroup, UUID> {

    // Retorna APENAS os IDs dos grupos que o usuário desativou. (Muito rápido e leve!)
    @Query("SELECT d.operationGroup.id FROM DeactivatedOperationGroup d WHERE d.user.id = :userId")
    Set<UUID> findDeactivatedGroupIdsByUserId(@Param("userId") String userId);

    // Usado para o botão de Ativar/Desativar
    Optional<DeactivatedOperationGroup> findByUserIdAndOperationGroupId(String userId, UUID groupId);
}
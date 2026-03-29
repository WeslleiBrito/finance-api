package com.project.financeapi.repository;

import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.enumSystem.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationGroupRepository
        extends JpaRepository<OperationGroup, UUID> {

    // ===== Acesso seguro =====
    Optional<OperationGroup> findByIdAndStatus(UUID id, StatusEntity status);

    @Query("""
                SELECT g
                FROM OperationGroup g
                WHERE LOWER(g.name) = LOWER(:name)
                  AND (g.isSystem is true OR g.createdBy.id = :userId)
            """)
    Optional<OperationGroup> findAccessibleByName(
            @Param("userId") String userId,
            @Param("name") String name
    );

    @Query("""
                SELECT g
                FROM OperationGroup g
                WHERE g.id = :id AND (g.createdBy.id = :userId or g.isSystem is true)
            """)
    Optional<OperationGroup> findByUserIdAndId(
            @Param("userId") String userId,
            @Param("id") UUID id
    );

    @Query("""
                SELECT g
                FROM OperationGroup g
                WHERE g.isSystem = true OR g.createdBy.id = :userId
            """)
    List<OperationGroup> findAllOperationGroupUserId(
            @Param("userId") String userId
    );
}

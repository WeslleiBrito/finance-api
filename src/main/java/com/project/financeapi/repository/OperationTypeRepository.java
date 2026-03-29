package com.project.financeapi.repository;

import com.project.financeapi.entity.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationTypeRepository
        extends JpaRepository<OperationType, UUID> {


    @Query("""
                SELECT t
                FROM OperationType t
                WHERE LOWER(t.name) = LOWER(:name)
                  AND (t.isSystem is true OR t.createdBy.id = :userId)
                  AND t.group.id = :groupId
            """)
    Optional<OperationType> findAccessibleByName(
            @Param("userId") String userId,
            @Param("groupId") UUID groupId,
            @Param("name") String name
    );

    @Query("""
                SELECT t
                FROM OperationType t
                WHERE t.id = :id AND (t.createdBy.id = :userId or t.isSystem is true)
            """)
    Optional<OperationType> findByUserIdAndId(
            @Param("userId") String userId,
            @Param("id") UUID id
    );

    @Query("""
                SELECT t
                FROM OperationType t
                WHERE t.isSystem = true OR t.createdBy.id = :userId
            """)
    List<OperationType> findAllOperationTypeUserId(
            @Param("userId") String userId
    );
}


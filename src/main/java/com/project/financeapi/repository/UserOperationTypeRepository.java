package com.project.financeapi.repository;

import com.project.financeapi.entity.OperationType;
import com.project.financeapi.entity.UserOperationType;
import com.project.financeapi.entity.UserOperationTypeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserOperationTypeRepository
        extends JpaRepository<UserOperationType, UserOperationTypeId> {

    @Query("""
        SELECT uot.operationType
        FROM UserOperationType uot
        WHERE (uot.user.id = :userId OR uot.operationType.isSystem = true)
        AND uot.enabled = :enabled
    """)
    List<OperationType> findAllByUserIdEnabled(
            @Param("userId") UUID userId,
            @Param("enabled") boolean enabled
    );

    @Query("""
        select uot.operationType
        from UserOperationType uot
        where uot.user.id = :userId
          and uot.operationType.group.id = :groupId
    """)
    List<OperationType> findVisibleTypesForUserAndGroup(
            @Param("userId") UUID userId,
            @Param("groupId") UUID groupId
    );

    Optional<UserOperationType> findByUserIdAndOperationTypeId(
            UUID userId,
            UUID operationTypeId
    );
}

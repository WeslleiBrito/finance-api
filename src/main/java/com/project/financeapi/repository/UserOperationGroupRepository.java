package com.project.financeapi.repository;

import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.UserOperationGroup;
import com.project.financeapi.entity.UserOperationGroupId;
import com.project.financeapi.enumSystem.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserOperationGroupRepository
        extends JpaRepository<UserOperationGroup, UserOperationGroupId> {

    @Query("""
        select uog.operationGroup
        from UserOperationGroup uog
        where (uog.user.id = :userId or uog.operationGroup.isSystem is true) 
        and uog.enabled = :enabled
    """)
    List<OperationGroup> findAllByUserOperationEnabled(
            @Param("userId") UUID userId,
            @Param("enabled") boolean enabled
    );

    @Query("""
        select uog.operationGroup
        from UserOperationGroup uog
        where uog.user.id = :userId
          and uog.enabled = true
          and uog.operationGroup.status = :statusEntity
        order by uog.operationGroup.name
    """)
    List<OperationGroup> findVisibleGroupsForUserStatus(
            @Param("userId") UUID userId,
            @Param("statusEntity") StatusEntity statusEntity
    );

    @Query("""
        select uog.operationGroup
        from UserOperationGroup uog
        where uog.operationGroup.id = :id
        AND (uog.user.id = :userId or uog.operationGroup.isSystem is true)
    """)
    Optional<OperationGroup> findByUserById(
            @Param("userId") UUID userId,
            @Param("id") UUID id
    );

    Optional<UserOperationGroup> findByUserIdAndOperationGroupId(
            UUID userId,
            UUID operationGroupId
    );

    boolean existsByUserIdAndOperationGroupId(
            UUID userId,
            UUID operationGroupId
    );
}


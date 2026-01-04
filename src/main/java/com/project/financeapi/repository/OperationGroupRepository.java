package com.project.financeapi.repository;

import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationGroupRepository extends JpaRepository<OperationGroup, UUID> {
    @Query("""
            SELECT g FROM OperationGroup g
            WHERE g.createdBy IS NULL OR g.createdBy = :user
    """)
    List<OperationGroup> findAllByUserOrDefault(@Param("user") User user);

    @Query("""
            SELECT g FROM OperationGroup g
            WHERE g.id = :id AND (g.createdBy IS NULL OR g.createdBy = :user)
    """)
    Optional<OperationGroup> findByCreatedByById(@Param("user") User user, @Param("id") UUID id);

    @Query("""
            SELECT g FROM OperationGroup g
            WHERE LOWER(g.name) = LOWER(:name) AND (g.createdBy IS NULL OR g.createdBy = :user)
    """)
    Optional<OperationGroup> findByCreatedByByName(@Param("user") User user, @Param("name") String name);

    @Query(
            """
                SELECT COUNT(g) > 0
                FROM OperationGroup g
                WHERE LOWER(g.name) = LOWER(:name) AND (g.createdBy IS NULL OR g.createdBy = :user)
            """
    )
    boolean nameExitsByCreatedBy(
            @Param("user") User user,
            @Param("name") String name
    );

    @Query(
            """
                SELECT g FROM OperationGroup g
                WHERE g.operationStatus = :operationStatus AND (g.createdBy IS NULL OR g.createdBy = :user)
            """
    )
    List<OperationGroup> findAllByUserOperationStatus(@Param("user") User user, @Param("operationStatus") OperationStatus operationStatus);

}
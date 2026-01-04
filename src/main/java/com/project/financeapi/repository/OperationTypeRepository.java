package com.project.financeapi.repository;

import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.OperationType;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationTypeRepository extends JpaRepository<OperationType, UUID> {

    @Query("""
            SELECT t FROM OperationType t
            WHERE t.createdBy IS NULL OR t.createdBy = :user
            ORDER BY t.name
    """)
    List<OperationType> findAllByUserOrDefault(@Param("user") User user);


    @Query("""
            SELECT t FROM OperationType t
            WHERE t.createdBy IS NULL OR t.createdBy = :user
            ORDER BY t.name
    """)
    List<OperationType> findByCreatedByAndMovementType(@Param("user") User user, MovementType movementType);


    @Query("""
            SELECT t FROM OperationType t
            WHERE t.createdBy IS NULL OR t.createdBy = :user
            ORDER BY t.name
    """)
    List<OperationType> findByCreatedByAndOperationStatus(@Param("user") User user, OperationStatus operationStatus);

    @Query("""
            SELECT t FROM OperationType t
            WHERE t.createdBy IS NULL OR t.createdBy = :user
            ORDER BY t.name
    """)
    List<OperationType> findByCreatedByAndGroup(@Param("user") User user, OperationGroup operationGroup);

    @Query(
            """
                SELECT t FROM OperationType t
                WHERE (t.createdBy IS NULL OR t.createdBy = :user)
                AND t.id = :id
            """
    )
    Optional<OperationType> findByCreatedByAndId(@Param("user") User user, @Param("id") UUID id);

    @Query(
            """
                SELECT p FROM OperationType p
                WHERE p.operationStatus = :operationStatus AND (p.createdBy IS NULL OR p.createdBy = :user)
            """
    )
    List<OperationType> findAllByUserOperationStatus(@Param("user") User user, @Param("operationStatus") OperationStatus operationStatus);

    @Query(
            """
                SELECT COUNT(p) > 0
                FROM OperationType p
                WHERE LOWER(p.name) = LOWER(:name)
                AND (p.createdBy.id IS NULL OR p.createdBy.id = :userid)
                AND p.group.id = :operationGroupId
            """
    )
    boolean existsByCreatedBy_IdAndNameAndOperationGroup_Id(
            @Param("userId") UUID userId,
            @Param("name") String name,
            @Param("operationGroupId") UUID operationGroupId
    );

}

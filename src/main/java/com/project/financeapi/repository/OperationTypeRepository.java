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

public interface OperationTypeRepository extends JpaRepository<OperationType, String> {

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

    Optional<OperationType> findByCreatedByAndId(User user, String id);

}

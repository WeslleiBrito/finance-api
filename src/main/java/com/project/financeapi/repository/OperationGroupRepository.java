package com.project.financeapi.repository;

import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.User;
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
            ORDER BY g.name
    """)
    List<OperationGroup> findAllByUserOrDefault(@Param("user") User user);

    Optional<OperationGroup> findByCreatedBy(User user);

}

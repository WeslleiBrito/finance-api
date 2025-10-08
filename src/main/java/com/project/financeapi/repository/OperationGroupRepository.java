package com.project.financeapi.repository;

import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OperationGroupRepository extends JpaRepository<OperationGroup, String> {
    @Query("""
            SELECT g FROM OperationGroup g
            WHERE g.createdBy IS NULL OR g.createdBy = :user
            ORDER BY g.name
    """)
    List<OperationGroup> findAllByUserOrDefault(@Param("user") User user);
}

package com.project.financeapi.repository;

import com.project.financeapi.entity.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentRepository extends JpaRepository<Installment, String> {
}

package com.project.financeapi.repository;

import com.project.financeapi.entity.LegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalEntityRepository extends JpaRepository<LegalEntity, String> {
}

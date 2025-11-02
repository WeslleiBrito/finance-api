package com.project.financeapi.repository;

import com.project.financeapi.entity.LegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LegalEntityRepository extends JpaRepository<LegalEntity, UUID> {
}

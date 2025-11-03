package com.project.financeapi.repository;

import com.project.financeapi.entity.PhysicalPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PhysicalPersonRepository extends JpaRepository<PhysicalPerson, UUID> {
}

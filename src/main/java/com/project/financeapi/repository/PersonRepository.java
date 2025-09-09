package com.project.financeapi.repository;

import com.project.financeapi.entity.base.PersonBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonBase, String> {
}

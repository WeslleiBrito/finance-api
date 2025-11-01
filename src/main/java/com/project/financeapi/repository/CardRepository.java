package com.project.financeapi.repository;

import com.project.financeapi.entity.base.CardBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<CardBase, String> {
}

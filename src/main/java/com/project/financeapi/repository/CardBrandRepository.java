package com.project.financeapi.repository;

import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardBrandRepository extends JpaRepository<CardBrand, UUID> {

    Optional<CardBrand> findByCreatedByAndId(User user, UUID id);

    List<CardBrand> findAllByCreatedBy(User user);

}

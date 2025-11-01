package com.project.financeapi.repository;

import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardBrandRepository extends JpaRepository<CardBrand, String> {

    Optional<CardBrand> findByCreatedByAndId(User user, String id);

    List<CardBrand> findAllByCreatedBy(User user);

}

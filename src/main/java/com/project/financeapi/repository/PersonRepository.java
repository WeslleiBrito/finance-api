package com.project.financeapi.repository;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PersonBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<PersonBase, String> {

    List<PersonBase> findByCreatedBy(User user);

    Optional<PersonBase> findByIdAndCreatedBy(String id, User user);
}

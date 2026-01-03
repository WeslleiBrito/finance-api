package com.project.financeapi.repository;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PersonBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<PersonBase, UUID> {

    List<PersonBase> findByCreatedBy(User user);

    Optional<PersonBase> findByIdAndCreatedBy(UUID id, User user);

}

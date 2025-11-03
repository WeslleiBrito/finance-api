package com.project.financeapi.repository;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountBase, UUID> {

    List<AccountBase> findByAccountHolder(User user);

    Optional<AccountBase> findByAccountHolderAndId(User user, UUID id);
}

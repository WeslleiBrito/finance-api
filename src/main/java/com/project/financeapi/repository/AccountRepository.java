package com.project.financeapi.repository;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountBase, String> {

    List<AccountBase> findByAccountHolder(User user);
}

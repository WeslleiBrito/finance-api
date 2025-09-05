package com.project.financeapi.repository;

import com.project.financeapi.entity.base.AccountBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountBase, String> {
}

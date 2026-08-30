package com.project.financeapi.repository;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountBase, UUID> {

    List<AccountBase> findByAccountHolder(User user);

    Optional<AccountBase> findByAccountHolderAndId(User user, UUID id);

    // 🌟 NOVO: Busca a conta e aplica o LOCK PESSIMISTA na linha do banco de dados
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountBase a WHERE a.id = :id")
    Optional<AccountBase> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            SELECT COUNT(a) > 0
            FROM AccountBase a
            WHERE LOWER(a.name) = LOWER(:name) AND (a.accountHolder.id IS NULL OR a.accountHolder.id = :userId)
           """
    )
    boolean nameExitsByAccountHolderId(
            @Param("name") String name,
            @Param("userId") String userId
    );
}
package com.project.financeapi.repository;

import com.project.financeapi.entity.DeactivatedCardBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DeactivatedCardBrandRepository extends JpaRepository<DeactivatedCardBrand, UUID> {
    @Query("SELECT d.cardBrand.id FROM DeactivatedCardBrand d WHERE d.user.id = :userId")
    Set<UUID> findDeactivatedBrandIdsByUserId(@Param("userId") String userId);

    Optional<DeactivatedCardBrand> findByUserIdAndCardBrandId(String userId, UUID cardBrandId);
}
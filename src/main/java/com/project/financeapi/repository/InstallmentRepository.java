package com.project.financeapi.repository;

import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.Installment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.project.financeapi.enumSystem.MovementDirection;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.project.financeapi.dto.dashboard.SnowballProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    public List<Installment> findByInvoice(Invoice invoice);

    @Query("""
    SELECT i FROM Installment i
    WHERE i.id = :id AND i.createdBy.id = :userId
    """)
    Optional<Installment> findCreditCardByCreatedByAndId(UUID userId, UUID id);

    // 🌟 NOVO: Busca a parcela e aplica o LOCK PESSIMISTA na linha do banco de dados
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Installment i WHERE i.id = :id")
    Optional<Installment> findByIdForUpdate(@Param("id") UUID id);

    // Calcula o valor pendente (amount - total_paid calculado) das parcelas em aberto
    @Query(value = "SELECT COALESCE(SUM(i.amount - (SELECT COALESCE(SUM(CASE WHEN t.movement_type = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM transactions t WHERE t.installment_id = i.id)), 0) " +
            "FROM installments i " +
            "WHERE i.created_by = :userId " +
            "AND i.movement_direction = :direction " +
            "AND i.amount > (SELECT COALESCE(SUM(CASE WHEN t.movement_type = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM transactions t WHERE t.installment_id = i.id)", nativeQuery = true)
    BigDecimal sumPendingAmountByDirection(@Param("userId") String userId, @Param("direction") String direction);

    // Conta as parcelas em aberto que vencem até a data limite
    @Query(value = "SELECT COUNT(i.id) FROM installments i " +
            "WHERE i.created_by = :userId " +
            "AND i.due_date <= :limitDate " +
            "AND i.amount > (SELECT COALESCE(SUM(CASE WHEN t.movement_type = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM transactions t WHERE t.installment_id = i.id)", nativeQuery = true)
    long countInstallmentsDueUpTo(@Param("userId") String userId, @Param("limitDate") LocalDate limitDate);

    @Query("SELECT i FROM Installment i " +
            "WHERE i.createdBy.id = :userId AND i.movementDirection = :direction " +
            "AND (:searchName IS NULL OR LOWER(i.invoice.person.name) LIKE LOWER(CONCAT('%', CAST(:searchName AS string), '%'))) " +
            "AND (:accountId IS NULL OR i.account.id = :accountId) " +
            "AND (:instrumentId IS NULL OR i.paymentInstrument.id = :instrumentId) " +
            "AND (CAST(:startDate AS date) IS NULL OR i.dueDate >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR i.dueDate <= :endDate) " +
            "AND (" +
            "  :statusFilter = 'ALL' " +
            "  OR (:statusFilter = 'PAID' AND i.amount <= (SELECT COALESCE(SUM(CASE WHEN t.movementType = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM Transaction t WHERE t.installment.id = i.id)) " +
            "  OR (:statusFilter = 'UPCOMING' AND i.amount > (SELECT COALESCE(SUM(CASE WHEN t.movementType = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM Transaction t WHERE t.installment.id = i.id) AND i.dueDate >= CURRENT_DATE) " +
            "  OR (:statusFilter = 'OVERDUE' AND i.amount > (SELECT COALESCE(SUM(CASE WHEN t.movementType = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM Transaction t WHERE t.installment.id = i.id) AND i.dueDate < CURRENT_DATE)" +
            ")" +
            "ORDER BY i.dueDate ASC"
    )
    Page<Installment> searchInstallments(
            @Param("userId") String userId,
            @Param("direction") MovementDirection direction,
            @Param("searchName") String searchName,
            @Param("accountId") UUID accountId,
            @Param("instrumentId") UUID instrumentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statusFilter") String statusFilter,
            Pageable pageable
    );

    // Mesma query, mas retorna a lista inteira (sem paginação) para o Java processar a soma exata
    @Query("SELECT i FROM Installment i " +
            "WHERE i.createdBy.id = :userId AND i.movementDirection = :direction " +
            "AND (:searchName IS NULL OR LOWER(i.invoice.person.name) LIKE LOWER(CONCAT('%', CAST(:searchName AS string), '%'))) " +
            "AND (:accountId IS NULL OR i.account.id = :accountId) " +
            "AND (:instrumentId IS NULL OR i.paymentInstrument.id = :instrumentId) " +
            "AND (CAST(:startDate AS date) IS NULL OR i.dueDate >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR i.dueDate <= :endDate) " +
            "AND (" +
            "  :statusFilter = 'ALL' " +
            "  OR (:statusFilter = 'PAID' AND i.amount <= (SELECT COALESCE(SUM(CASE WHEN t.movementType = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM Transaction t WHERE t.installment.id = i.id)) " +
            "  OR (:statusFilter = 'UPCOMING' AND i.amount > (SELECT COALESCE(SUM(CASE WHEN t.movementType = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM Transaction t WHERE t.installment.id = i.id) AND i.dueDate >= CURRENT_DATE) " +
            "  OR (:statusFilter = 'OVERDUE' AND i.amount > (SELECT COALESCE(SUM(CASE WHEN t.movementType = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM Transaction t WHERE t.installment.id = i.id) AND i.dueDate < CURRENT_DATE)" +
            ")" +
            "ORDER BY i.dueDate ASC "
    )
    List<Installment> searchInstallmentsUnpaginated(
            @Param("userId") String userId,
            @Param("direction") MovementDirection direction,
            @Param("searchName") String searchName,
            @Param("accountId") UUID accountId,
            @Param("instrumentId") UUID instrumentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statusFilter") String statusFilter
    );

    // Consulta nativa no PostgreSQL para o Gráfico Bola de Neve
    // Agrupa o saldo devedor (amount - total_paid) das faturas de cartão de crédito por mês
    @Query(value = "SELECT TO_CHAR(i.due_date, 'YYYY-MM') AS month, " +
            "SUM(i.amount - (SELECT COALESCE(SUM(CASE WHEN t.movement_type = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM transactions t WHERE t.installment_id = i.id)) AS total " +
            "FROM installments i " +
            "JOIN payment_instrument p ON i.payment_instrument = p.id " +
            "WHERE i.created_by = :userId " +
            "AND p.payment_type = 'CREDIT_CARD' " +
            "AND TO_CHAR(i.due_date, 'YYYY-MM') >= :currentMonth " +
            "AND i.amount > (SELECT COALESCE(SUM(CASE WHEN t.movement_type = 'REVERSAL' THEN -t.amount ELSE t.amount END), 0) FROM transactions t WHERE t.installment_id = i.id) " +
            "GROUP BY TO_CHAR(i.due_date, 'YYYY-MM') " +
            "ORDER BY month DESC " +
            "LIMIT 6", nativeQuery = true)
    List<SnowballProjection> getCreditCardSnowballChart(@Param("userId") String userId, @Param("currentMonth") String currentMonth);
}
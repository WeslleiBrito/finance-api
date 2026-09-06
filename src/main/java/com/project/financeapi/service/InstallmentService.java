package com.project.financeapi.service;

import com.project.financeapi.dto.Installments.InstallmentResponseDTO;
import com.project.financeapi.dto.dashboard.InstallmentSummaryDTO;
import com.project.financeapi.dto.dashboard.InstallmentSummaryDTO.MonthlyChartDTO;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstallmentService {

    private final InstallmentRepository installmentRepository;
    private final UserContextService userContextService;

    public Page<InstallmentResponseDTO> searchInstallments(
            MovementDirection direction, String searchName, UUID accountId, UUID instrumentId,
            LocalDate startDate, LocalDate endDate, String statusFilter, Pageable pageable) {

        String userId = userContextService.getAuthenticatedUser().getId();
        return installmentRepository.searchInstallments(
                userId, direction, searchName, accountId, instrumentId, startDate, endDate, statusFilter, pageable
        ).map(Installment::toResponse);
    }

    public InstallmentSummaryDTO getSummary(
            MovementDirection direction, String searchName, UUID accountId, UUID instrumentId,
            LocalDate startDate, LocalDate endDate, String statusFilter) {

        String userId = userContextService.getAuthenticatedUser().getId();

        List<Installment> list = installmentRepository.searchInstallmentsUnpaginated(
                userId, direction, searchName, accountId, instrumentId, startDate, endDate, statusFilter);

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalThisMonth = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        BigDecimal totalOpen = BigDecimal.ZERO;

        Map<YearMonth, BigDecimal> chartMap = new TreeMap<>();
        LocalDate today = LocalDate.now();

        for (Installment i : list) {
            BigDecimal paid = i.getTotalPaid();
            BigDecimal remaining = i.getRemainingBalance();

            totalPaid = totalPaid.add(paid);

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                totalOpen = totalOpen.add(remaining);

                if (i.getDueDate().isBefore(today)) {
                    totalOverdue = totalOverdue.add(remaining);
                }
                if (i.getDueDate().getYear() == today.getYear() && i.getDueDate().getMonth() == today.getMonth()) {
                    totalThisMonth = totalThisMonth.add(remaining);
                }

                // Agrupa para o gráfico
                YearMonth monthKey = YearMonth.from(i.getDueDate());
                chartMap.put(monthKey, chartMap.getOrDefault(monthKey, BigDecimal.ZERO).add(remaining));
            }
        }

        List<MonthlyChartDTO> chartData = chartMap.entrySet().stream()
                .map(e -> new MonthlyChartDTO(e.getKey().format(DateTimeFormatter.ofPattern("MM/yyyy")), e.getValue()))
                .toList();

        return new InstallmentSummaryDTO(totalPaid, totalThisMonth, totalOverdue, totalOpen, chartData);
    }
}
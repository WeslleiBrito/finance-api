package com.project.financeapi.service;

import com.project.financeapi.dto.dashboard.DashboardSummaryDTO;
import com.project.financeapi.dto.dashboard.DashboardSummaryDTO.*;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.MovementType;
import com.project.financeapi.repository.AccountRepository;
import com.project.financeapi.repository.InstallmentRepository;
import com.project.financeapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountRepository accountRepository;
    private final InstallmentRepository installmentRepository;
    private final TransactionRepository transactionRepository;
    private final UserContextService userContextService;

    public DashboardSummaryDTO getSummary() {
        User user = userContextService.getAuthenticatedUser();
        String userId = user.getId();

        // 1. Agregações SQL diretas e rápidas
        BigDecimal totalBalance = accountRepository.sumTotalBalanceByUserId(userId);
        BigDecimal toReceive = installmentRepository.sumPendingAmountByDirection(userId, MovementDirection.INFLOW.name());
        BigDecimal toPay = installmentRepository.sumPendingAmountByDirection(userId, MovementDirection.OUTFLOW.name());
        long dueSoonCount = installmentRepository.countInstallmentsDueUpTo(userId, LocalDate.now().plusDays(15));

        // 2. Busca transações dos últimos 6 meses para o Gráfico e o Raio-X
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        List<Transaction> transactions = transactionRepository.findTransactionsForDashboard(userId, sixMonthsAgo);

        // 3. Monta o Gráfico Mensal
        Map<String, MonthlyChartDTO> chartMap = new TreeMap<>();
        for (Transaction t : transactions) {
            String month = t.getPaymentDate().format(DateTimeFormatter.ofPattern("MM/yy"));
            chartMap.putIfAbsent(month, new MonthlyChartDTO(month, BigDecimal.ZERO, BigDecimal.ZERO));

            MonthlyChartDTO current = chartMap.get(month);

            // Filtro estrito: Entradas = RECEIPT, Saídas = PAYMENT
            if (t.getMovementDirection() == MovementDirection.INFLOW && t.getMovementType() == MovementType.RECEIPT) {
                chartMap.put(month, new MonthlyChartDTO(month, current.entradas().add(t.getEffectiveAmount()), current.saidas()));
            } else if (t.getMovementDirection() == MovementDirection.OUTFLOW && t.getMovementType() == MovementType.PAYMENT) {
                chartMap.put(month, new MonthlyChartDTO(month, current.entradas(), current.saidas().add(t.getEffectiveAmount())));
            }
        }

        // 4. Monta o Raio-X do Mês Atual (Agrupamentos)
        String currentMonthStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Transaction> currentMonthTransactions = transactions.stream()
                .filter(t -> t.getPaymentDate().toString().startsWith(currentMonthStr))
                .toList();

        return new DashboardSummaryDTO(
                totalBalance,
                toReceive,
                toPay,
                dueSoonCount,
                groupByCategory(currentMonthTransactions, MovementDirection.OUTFLOW, MovementType.PAYMENT),
                groupByInstrument(currentMonthTransactions, MovementDirection.OUTFLOW, MovementType.PAYMENT),
                groupByCategory(currentMonthTransactions, MovementDirection.INFLOW, MovementType.RECEIPT),
                groupByInstrument(currentMonthTransactions, MovementDirection.INFLOW, MovementType.RECEIPT),
                new ArrayList<>(chartMap.values())
        );
    }

    private List<CategoryTotalDTO> groupByCategory(List<Transaction> transactions, MovementDirection direction, MovementType type) {
        Map<String, BigDecimal> grouped = transactions.stream()
                .filter(t -> t.getMovementDirection() == direction && t.getMovementType() == type)
                .collect(Collectors.groupingBy(
                        t -> {
                            if (t.getInstallment() != null && t.getInstallment().getInvoice() != null && t.getInstallment().getInvoice().getOperationType() != null) {
                                return t.getInstallment().getInvoice().getOperationType().getName();
                            }
                            return "Outros";
                        },
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getEffectiveAmount, BigDecimal::add)
                ));

        return grouped.entrySet().stream()
                .map(e -> new CategoryTotalDTO(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.value().compareTo(a.value()))
                .toList();
    }

    private List<InstrumentTotalDTO> groupByInstrument(List<Transaction> transactions, MovementDirection direction, MovementType type) {
        Map<String, BigDecimal> grouped = transactions.stream()
                .filter(t -> t.getMovementDirection() == direction && t.getMovementType() == type)
                .collect(Collectors.groupingBy(
                        t -> t.getPaymentInstrument() != null ? t.getPaymentInstrument().getName() : "Saldo da Conta / Dinheiro",
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getEffectiveAmount, BigDecimal::add)
                ));

        return grouped.entrySet().stream()
                .map(e -> new InstrumentTotalDTO(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.value().compareTo(a.value()))
                .toList();
    }
}
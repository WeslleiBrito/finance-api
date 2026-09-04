package com.project.financeapi.controller;

import com.project.financeapi.dto.investment.FixedIncomeDashboardDTO;
import com.project.financeapi.dto.investment.InvestmentApportDTO;
import com.project.financeapi.dto.investment.InvestmentRescueDTO;
import com.project.financeapi.service.FixedIncomeService;
import com.project.financeapi.service.InvestmentLedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class FixedIncomeController {

    private final FixedIncomeService fixedIncomeService;
    private final InvestmentLedgerService investmentLedgerService;

    @PostMapping("/apport")
    public ResponseEntity<Void> createApport(@RequestBody @Valid InvestmentApportDTO dto) {
        fixedIncomeService.createApport(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/rescue")
    public ResponseEntity<Void> executeRescue(@RequestBody @Valid InvestmentRescueDTO dto) {
        fixedIncomeService.executeRescue(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account/{accountId}/dashboards")
    public ResponseEntity<List<FixedIncomeDashboardDTO>> getAllDashboardsByAccount(@PathVariable UUID accountId) {
        List<FixedIncomeDashboardDTO> dashboards = investmentLedgerService.getAllActiveDashboardsByAccount(accountId);
        return ResponseEntity.ok(dashboards);
    }

    @GetMapping("/{fixedIncomeId}/dashboard")
    public ResponseEntity<FixedIncomeDashboardDTO> getDashboard(@PathVariable UUID fixedIncomeId) {
        FixedIncomeDashboardDTO dashboard = investmentLedgerService.getDashboard(fixedIncomeId);
        return ResponseEntity.ok(dashboard);
    }
}
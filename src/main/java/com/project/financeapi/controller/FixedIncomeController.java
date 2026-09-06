package com.project.financeapi.controller;

import com.project.financeapi.dto.investment.request.InvestmentApportDTO;
import com.project.financeapi.dto.investment.request.InvestmentRescueDTO;
import com.project.financeapi.dto.investment.response.FixedIncomeDashboardDTO;
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

    // ========================================================================
    // QUERIES (Leitura do Event Sourcing)
    // ========================================================================

    @GetMapping("/account/{accountId}/dashboards")
    public ResponseEntity<List<FixedIncomeDashboardDTO>> getDashboardsByAccount(@PathVariable UUID accountId) {
        List<FixedIncomeDashboardDTO> dashboards = investmentLedgerService.getAllActiveDashboardsByAccount(accountId);
        return ResponseEntity.ok(dashboards);
    }

    // ========================================================================
    // COMMANDS (Geração de Eventos)
    // ========================================================================

    @PostMapping("/apport")
    public ResponseEntity<FixedIncomeDashboardDTO> createApport(@RequestBody @Valid InvestmentApportDTO dto) {
        // 1. Executa a mutação e pega o ID do papel
        UUID fixedIncomeId = fixedIncomeService.createApport(dto);

        // 2. Lê o estado projetado atualizado
        FixedIncomeDashboardDTO dashboard = investmentLedgerService.getDashboard(fixedIncomeId);

        // 3. Devolve a nova inserção completa para o front-end
        return ResponseEntity.status(HttpStatus.CREATED).body(dashboard);
    }

    @PostMapping("/rescue")
    public ResponseEntity<FixedIncomeDashboardDTO> executeRescue(@RequestBody @Valid InvestmentRescueDTO dto) {
        // 1. Gera o evento de saída e muta a conta corrente
        fixedIncomeService.executeRescue(dto);

        // 2. Devolve o dashboard atualizado após o resgate
        FixedIncomeDashboardDTO dashboard = investmentLedgerService.getDashboard(dto.fixedIncomeId());
        return ResponseEntity.ok(dashboard);
    }
}
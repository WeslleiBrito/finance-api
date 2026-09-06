package com.project.financeapi.controller;

import com.project.financeapi.dto.dashboard.DashboardSummaryDTO;
import com.project.financeapi.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dados agregados e consolidados para a tela inicial")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Resumo financeiro", description = "Retorna os totais de contas, parcelas em aberto e dados dos gráficos do mês atual.")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.status(HttpStatus.OK).body(dashboardService.getSummary());
    }
}
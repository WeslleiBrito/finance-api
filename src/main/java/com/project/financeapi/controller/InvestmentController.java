package com.project.financeapi.controller;

import com.project.financeapi.dto.investment.request.CreateBoxDTO;
import com.project.financeapi.dto.investment.request.CreateProductDTO;
import com.project.financeapi.dto.investment.request.InvestmentApportDTO;
import com.project.financeapi.dto.investment.request.InvestmentRescueDTO;
import com.project.financeapi.dto.investment.response.ProductDashboardDTO;
import com.project.financeapi.service.InvestmentLedgerService;
import com.project.financeapi.service.InvestmentOperationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentOperationService operationService;
    private final InvestmentLedgerService ledgerService;

    // --- ENDPOINT 1: Criar Produto ---
    @PostMapping("/products")
    public ResponseEntity<ProductDashboardDTO> createProduct(@RequestBody @Valid CreateProductDTO dto) {
        ProductDashboardDTO dashboard = operationService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dashboard);
    }

    // --- ENDPOINT 2: Criar Caixinha / Objetivo ---
    @PostMapping("/boxes")
    public ResponseEntity<ProductDashboardDTO> createBox(@RequestBody @Valid CreateBoxDTO dto) {
        ProductDashboardDTO dashboard = operationService.createBox(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dashboard);
    }

    // --- ENDPOINT 3: Aporte de Capital ---
    @PostMapping("/apport")
    public ResponseEntity<Map<String, String>> executeApport(@RequestBody @Valid InvestmentApportDTO dto) {
        operationService.executeApport(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Aporte realizado com sucesso"
        ));
    }

    /**
     * Endpoint para Resgate.
     */
    @PostMapping("/rescue")
    public ResponseEntity<Map<String, String>> executeRescue(@RequestBody InvestmentRescueDTO dto) {
        operationService.executeRescue(dto);
        return ResponseEntity.ok(Map.of(
                "message", "Resgate processado com sucesso"
        ));
    }

    /**
     * Leitura do Dashboard Completo.
     */
    @GetMapping("/account/{accountId}/dashboards")
    public ResponseEntity<List<ProductDashboardDTO>> getDashboardsByAccount(@PathVariable UUID accountId) {
        List<ProductDashboardDTO> dashboards = ledgerService.getDashboardsByAccount(accountId);
        return ResponseEntity.ok(dashboards);
    }
}
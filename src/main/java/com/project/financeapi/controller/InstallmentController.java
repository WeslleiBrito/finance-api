package com.project.financeapi.controller;

import com.project.financeapi.dto.Installments.InstallmentResponseDTO;
import com.project.financeapi.dto.dashboard.InstallmentSummaryDTO;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.service.InstallmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/installments")
@RequiredArgsConstructor
public class InstallmentController {

    private final InstallmentService installmentService;

    @GetMapping("/search")
    public ResponseEntity<Page<InstallmentResponseDTO>> search(
            @RequestParam String direction,
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID instrumentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") String statusFilter,
            Pageable pageable) {

        MovementDirection dirEnum = MovementDirection.valueOf(direction.toUpperCase());

        return ResponseEntity.ok(installmentService.searchInstallments(
                dirEnum, searchName, accountId, instrumentId, startDate, endDate, statusFilter, pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<InstallmentSummaryDTO> getSummary(
            @RequestParam String direction,
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID instrumentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") String statusFilter) {

        MovementDirection dirEnum = MovementDirection.valueOf(direction.toUpperCase());

        return ResponseEntity.ok(installmentService.getSummary(
                dirEnum, searchName, accountId, instrumentId, startDate, endDate, statusFilter));
    }
}
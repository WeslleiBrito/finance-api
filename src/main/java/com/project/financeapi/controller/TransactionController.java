package com.project.financeapi.controller;

import com.project.financeapi.dto.transaction.CreateTransactionRequestDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transações", description = "Registro e processamento de transações financeiras")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    @Operation(summary = "Cria novas transações")
    public ResponseEntity<List<TransactionResponseDTO>> create(
            @Valid @RequestBody CreateTransactionRequestDTO transactions
    ) {
        List<TransactionResponseDTO> responses = transactionService.createCommonTransactions(transactions);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // 🌟 ROTA ADICIONADA: Busca o extrato do usuário logado (GET /api/transactions)
    @GetMapping
    @Operation(summary = "Listar extrato", description = "Busca as transações referentes ao usuário autenticado.")
    public ResponseEntity<List<TransactionResponseDTO>> findAll() {
        List<TransactionResponseDTO> transactions = transactionService.findAllByUser();
        return ResponseEntity.status(HttpStatus.OK).body(transactions);
    }

    // 🌟 ROTA ADICIONADA: Estorno (POST /api/transactions/{id}/reverse)
    @PostMapping("/{id}/reverse")
    @Operation(summary = "Estornar transação", description = "Cria uma transação de estorno e recalcula os saldos.")
    public ResponseEntity<TransactionResponseDTO> reverseTransaction(
            @PathVariable UUID id
    ) {
        TransactionResponseDTO response = transactionService.reverseTransaction(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
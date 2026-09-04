package com.project.financeapi.controller;

import com.project.financeapi.dto.transaction.CreateManualAdjustmentTransactionRequestDTO;
import com.project.financeapi.dto.transaction.CreateTransactionRequestDTO;
import com.project.financeapi.dto.transaction.ReversalRequestDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDate;
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
    @Operation(summary = "Listar extrato", description = "Busca as transações referentes ao usuário autenticado de forma paginada.")
    public ResponseEntity<Page<TransactionResponseDTO>> findAll(
            @PageableDefault(page = 0, size = 20, sort = "paymentDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TransactionResponseDTO> transactions = transactionService.findAllByUser(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(transactions);
    }

    @PostMapping("/adjustments")
    @Operation(summary = "Cria lançamentos/ajustes manuais", description = "Cria transações avulsas que afetam diretamente o saldo da conta, sem vinculação com faturas.")
    public ResponseEntity<List<TransactionResponseDTO>> createAdjustments(
            @Valid @RequestBody CreateManualAdjustmentTransactionRequestDTO request
    ) {
        List<TransactionResponseDTO> responses = transactionService.createManualAdjustments(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PostMapping("/{id}/reverse")
    @Operation(summary = "Estornar transação", description = "Cria uma transação de estorno e recalcula os saldos.")
    public ResponseEntity<TransactionResponseDTO> reverseTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ReversalRequestDTO dto
    ) {
        TransactionResponseDTO response = transactionService.reverseTransaction(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transferência entre contas", description = "Transfere saldo de uma conta para outra através de transações espelhadas.")
    public ResponseEntity<List<TransactionResponseDTO>> transfer(
            @Valid @RequestBody com.project.financeapi.dto.transaction.TransferRequestDTO request
    ) {
        List<TransactionResponseDTO> responses = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponseDTO>> search(
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {

        // Tratamento de conversão HTTP -> Tipagem Java
        MovementDirection dirEnum = (direction != null && !direction.equalsIgnoreCase("ALL"))
                ? MovementDirection.valueOf(direction.toUpperCase())
                : null;

        // Delega para o serviço
        Page<TransactionResponseDTO> result = transactionService.searchTransactions(
                dirEnum, searchName, accountId, startDate, endDate, pageable
        );

        return ResponseEntity.ok(result);
    }

}
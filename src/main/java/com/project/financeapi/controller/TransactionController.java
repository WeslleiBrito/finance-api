package com.project.financeapi.controller;

import com.project.financeapi.dto.transaction.CreateTransactionRequestDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.service.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transações", description = "Registro e processamento de transações financeiras")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<List<TransactionResponseDTO>> create(
            @Valid @RequestBody CreateTransactionRequestDTO transactions
    ) {
        List<TransactionResponseDTO> responses = transactionService.createCommonTransactions(transactions);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
}

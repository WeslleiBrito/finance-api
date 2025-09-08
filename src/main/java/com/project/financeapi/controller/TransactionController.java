package com.project.financeapi.controller;

import com.project.financeapi.dto.transaction.TransactionRequestDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<List<TransactionResponseDTO>> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody TransactionRequestDTO dto
    ) {
        List<TransactionResponseDTO> responses = transactionService.create(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
}

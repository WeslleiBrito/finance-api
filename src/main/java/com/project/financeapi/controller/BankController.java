package com.project.financeapi.controller;

import com.project.financeapi.dto.bank.*;
import com.project.financeapi.dto.bank.BankUpdateRequestDTO;
import com.project.financeapi.enumSystem.BankStatus;
import com.project.financeapi.service.BankService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
@Tag(name = "Bancos", description = "Cadastro e manutenção de instituições bancárias")
public class BankController {

    private final BankService bankService;



    @PutMapping("/{id}")
    public ResponseEntity<BankResponseDTO> update(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody BankUpdateRequestDTO dto,
            @Valid @PathVariable UUID id
    ){
        BankResponseDTO bank = bankService.update(token, dto, id);

        return ResponseEntity.status(HttpStatus.OK).body(bank);
    }

    @GetMapping
    public ResponseEntity<List<BankResponseDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token
    ){
        return ResponseEntity.status(HttpStatus.OK).body(bankService.findAll(token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankResponseDTO> findById(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable UUID id
    ){
        return ResponseEntity.status(HttpStatus.OK).body(bankService.getById(token, id));
    }

    @GetMapping("/bank-status/{status}")
    public ResponseEntity<List<BankResponseDTO>> findAllOperationStatus(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable BankStatus status
    ){
        return ResponseEntity.status(HttpStatus.OK).body(bankService.findAllBankStatus(token, status));
    }
}

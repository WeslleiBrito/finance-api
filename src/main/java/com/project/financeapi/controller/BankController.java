package com.project.financeapi.controller;


import com.project.financeapi.dto.bank.BankCreateRequestDTO;
import com.project.financeapi.dto.bank.BankResponseDTO;
import com.project.financeapi.dto.bank.BankUpdateRequestDTO;
import com.project.financeapi.service.BankService;
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
public class BankController {

    private final BankService bankService;

    @PostMapping
    public ResponseEntity<BankResponseDTO> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody BankCreateRequestDTO dto
            ) {

        BankResponseDTO bank = bankService.create(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(bank);
    }

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
    public ResponseEntity<List<BankResponseDTO>> getAll(
            @RequestHeader("X-Auth-Token") String token
    ){
        return ResponseEntity.status(HttpStatus.OK).body(bankService.getAll(token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankResponseDTO> getById(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable UUID id
    ){
        return ResponseEntity.status(HttpStatus.OK).body(bankService.getById(token, id));
    }
}

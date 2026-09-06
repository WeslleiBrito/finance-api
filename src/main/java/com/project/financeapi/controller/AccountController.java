package com.project.financeapi.controller;

import com.project.financeapi.dto.account.*;
import com.project.financeapi.dto.account.create.*;
import com.project.financeapi.dto.account.response.*;
import com.project.financeapi.dto.account.update.*;
import com.project.financeapi.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Contas", description = "Gerenciamento de contas bancárias (Corrente, Poupança, Investimento, etc.)")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create/checking")
    public ResponseEntity<CreateCheckingAccountResponseDTO> createChecking(
            @Valid @RequestBody CreateCheckingAccountRequestDTO dto
    ){
        CreateCheckingAccountResponseDTO account = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/create/investment")
    public ResponseEntity<CreateInvestmentAccountResponseDTO> createInvestment(
            @Valid @RequestBody CreateInvestmentAccountRequestDTO dto
    ){
        CreateInvestmentAccountResponseDTO account = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/create/payment")
    public ResponseEntity<CreatePaymentAccountResponseDTO> createPayment(
            @Valid @RequestBody CreatePaymentAccountRequestDTO dto
    ){
        CreatePaymentAccountResponseDTO account = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/create/savings")
    public ResponseEntity<CreateSavingsAccountResponseDTO> createSavings(
            @Valid @RequestBody CreateSavingsAccountRequestDTO dto
    ){
        CreateSavingsAccountResponseDTO account = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/create/wallet")
    public ResponseEntity<CreateWalletAccountResponseDTO> createWallet(
            @Valid @RequestBody CreateWalletAccountRequestDTO dto
    ){
        CreateWalletAccountResponseDTO account = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponseDTO>> findAll(
            @PageableDefault(page = 0, size = 20, sort = "name") Pageable pageable
    ){
        Page<AccountResponseDTO> accounts = accountService.findAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }

    @PutMapping("/update/checking/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @Valid @RequestBody UpdateCheckingAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/update/investment/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @Valid @RequestBody UpdateInvestmentAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/update/payment/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/update/savings/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @Valid @RequestBody UpdateSavingsAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/update/wallet/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @Valid @RequestBody UpdateWalletAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PatchMapping("/update-status/{id}")
    public ResponseEntity<HttpStatus> updateStatus(
            @Valid @PathVariable UUID id

    ){
        accountService.updateStatus(id);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getById(
            @Valid @PathVariable UUID id
    ){
        AccountResponseDTO account = accountService.findById(id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(account);
    }
}

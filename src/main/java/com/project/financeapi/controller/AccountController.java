package com.project.financeapi.controller;

import com.project.financeapi.dto.account.*;
import com.project.financeapi.dto.account.create.*;
import com.project.financeapi.dto.account.response.*;
import com.project.financeapi.dto.account.update.*;
import com.project.financeapi.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create/checking")
    public ResponseEntity<CreateCheckingAccountResponseDTO> createChecking(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateCheckingAccountRequestDTO dto
    ){
        CreateCheckingAccountResponseDTO account = accountService.create(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/create/investment")
    public ResponseEntity<CreateInvestmentAccountResponseDTO> createInvestment(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateInvestmentAccountRequestDTO dto
    ){
        CreateInvestmentAccountResponseDTO account = accountService.create(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/create/payment")
    public ResponseEntity<CreatePaymentAccountResponseDTO> createPayment(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreatePaymentAccountRequestDTO dto
    ){
        CreatePaymentAccountResponseDTO account = accountService.create(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/create/savings")
    public ResponseEntity<CreateSavingsAccountResponseDTO> createSavings(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateSavingsAccountRequestDTO dto
    ){
        CreateSavingsAccountResponseDTO account = accountService.create(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/create/wallet")
    public ResponseEntity<CreateWalletAccountResponseDTO> createWallet(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateWalletAccountRequestDTO dto
    ){
        CreateWalletAccountResponseDTO account = accountService.create(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping
    public  ResponseEntity<List<AccountResponseDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token
    ){
        List<AccountResponseDTO> accounts = accountService.findAll(token);
        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }

    @PutMapping("/update/checking/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateCheckingAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(token, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/update/investment/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateInvestmentAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(token, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/update/payment/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdatePaymentAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(token, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/update/savings/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateSavingsAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(token, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/update/wallet/{id}")
    public ResponseEntity<AccountResponseDTO> update(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateWalletAccountRequestDTO dto
    ) {
        AccountResponseDTO account = accountService.update(token, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<ResponseDeactivateAccountDTO> deactivateAccount(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token
    ){
        ResponseDeactivateAccountDTO account = accountService.deactivateAccount(token, id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(account);
    }


    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getById(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token
    ){
        AccountResponseDTO account = accountService.findById(token, id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(account);
    }
}

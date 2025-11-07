package com.project.financeapi.controller;

import com.project.financeapi.dto.account.create.CreateAccountBase;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.account.ResponseDeactivateAccountDTO;
import com.project.financeapi.dto.account.UpdateAccountRequestDTO;
import com.project.financeapi.dto.account.create.CreateCheckingAccountRequestDTO;
import com.project.financeapi.dto.account.create.CreateInvestmentAccountRequestDTO;
import com.project.financeapi.dto.account.response.CreateCheckingAccountResponseDTO;
import com.project.financeapi.dto.account.response.CreateInvestmentAccountResponseDTO;
import com.project.financeapi.entity.account.CheckingAccount;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

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

    @GetMapping
    public  ResponseEntity<List<ResponseAccountDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token
    ){
        List<ResponseAccountDTO> accounts = accountService.findAll(token);
        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountBase> update(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateAccountRequestDTO dto
    ) {
        AccountBase account = accountService.update(token, id, dto);
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
    public ResponseEntity<ResponseAccountDTO> getById(
            @Valid @PathVariable UUID id,
            @RequestHeader("X-Auth-Token") String token
    ){
        ResponseAccountDTO account = accountService.findById(token, id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(account);
    }
}

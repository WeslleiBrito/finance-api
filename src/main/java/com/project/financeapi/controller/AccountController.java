package com.project.financeapi.controller;

import com.project.financeapi.dto.account.CreateAccountRequestDTO;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountBase> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateAccountRequestDTO dto
    ){
        AccountBase account = accountService.create(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
}

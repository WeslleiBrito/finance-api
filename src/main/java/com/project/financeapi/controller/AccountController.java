package com.project.financeapi.controller;

import com.project.financeapi.dto.account.CreateAccountRequestDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.account.ResponseDeactivateAccountDTO;
import com.project.financeapi.dto.account.UpdateAccountRequestDTO;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    @GetMapping
    public  ResponseEntity<List<ResponseAccountDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token
    ){
        List<ResponseAccountDTO> accounts = accountService.findAll(token);
        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountBase> update(
            @PathVariable String id,
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateAccountRequestDTO dto
    ) {
        AccountBase account = accountService.update(token, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<ResponseDeactivateAccountDTO> deactivateAccount(
            @PathVariable String id,
            @RequestHeader("X-Auth-Token") String token
    ){
        ResponseDeactivateAccountDTO account = accountService.deactivateAccount(token, id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(account);
    }
}

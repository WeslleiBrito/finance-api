package com.project.financeapi.controller;

import com.project.financeapi.dto.card.creditCard.CreditCardCreateRequestDTO;
import com.project.financeapi.dto.card.creditCard.CreditCardResponseDTO;
import com.project.financeapi.dto.card.creditCard.CreditCardUpdateRequestDTO;
import com.project.financeapi.service.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/credit-card")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    @PostMapping
    public ResponseEntity<CreditCardResponseDTO> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreditCardCreateRequestDTO dto
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(creditCardService.create(token, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditCardResponseDTO> update(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable UUID id,
            @Valid @RequestBody CreditCardUpdateRequestDTO dto
    ){
        return ResponseEntity.status(HttpStatus.OK).body(creditCardService.update(token, dto, id));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponseDTO>> getAll(
            @RequestHeader("X-Auth-Token") String token
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(creditCardService.getAll(token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardResponseDTO> getById(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable UUID id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(creditCardService.getById(token, id));
    }
}

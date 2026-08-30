package com.project.financeapi.controller;

import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.service.CreditCardService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cartões de Crédito (Consulta)", description = "Endpoints para listagem e detalhamento de cartões de crédito")
public class CreditCardController {

    private final CreditCardService creditCardService;


    @GetMapping
    public ResponseEntity<List<CreditCardDetailsDTO>> getAll(
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(creditCardService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardDetailsDTO> getById(
            @Valid @PathVariable UUID id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(creditCardService.getById(id));
    }
}

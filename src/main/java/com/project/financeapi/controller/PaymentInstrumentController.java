package com.project.financeapi.controller;

import com.project.financeapi.dto.card.creditCard.CreditCardCreateRequestDTO;
import com.project.financeapi.dto.card.creditCard.UpdateCreditCardRequestDTO;
import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.dto.payment.PaymentMethodDetailsDTO;
import com.project.financeapi.service.PaymentInstrumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-instruments")
@RequiredArgsConstructor
public class PaymentInstrumentController {

    private final PaymentInstrumentService paymentInstrumentService;

    /* ======================================================
       CREDIT CARD
       ====================================================== */

    @PostMapping("/credit-cards")
    public ResponseEntity<CreditCardDetailsDTO> createCreditCard(
            @RequestHeader("X-Auth-Token") String token,
            @RequestBody @Valid CreditCardCreateRequestDTO dto
    ) {
        CreditCardDetailsDTO response =
                paymentInstrumentService.createCreditCard(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/credit-cards/{id}/update")
    public ResponseEntity<CreditCardDetailsDTO> updateCreditCard(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCreditCardRequestDTO dto
    ) {
        CreditCardDetailsDTO response =
                paymentInstrumentService.updateCreditCard(token, dto, id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterStatus(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable UUID id
    ) {
        paymentInstrumentService.alterStatusInstrument(token, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodDetailsDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token
    ) {
        return ResponseEntity.ok(paymentInstrumentService.findAll(token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodDetailsDTO> findById(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(paymentInstrumentService.findById(token, id));
    }
}

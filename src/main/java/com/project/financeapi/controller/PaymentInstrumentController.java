package com.project.financeapi.controller;

import com.project.financeapi.dto.card.creditCard.CreditCardCreateRequestDTO;
import com.project.financeapi.dto.card.creditCard.UpdateCreditCardRequestDTO;
import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.dto.payment.PaymentMethodDetailsDTO;
import com.project.financeapi.service.PaymentInstrumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Instrumentos de Pagamento", description = "Criação e gerenciamento de métodos de pagamento (ex: Cartões de Crédito)")
public class PaymentInstrumentController {

    private final PaymentInstrumentService paymentInstrumentService;

    /* ======================================================
       CREDIT CARD
       ====================================================== */

    @PostMapping("/credit-cards")
    public ResponseEntity<CreditCardDetailsDTO> createCreditCard(
            @RequestBody @Valid CreditCardCreateRequestDTO dto
    ) {
        CreditCardDetailsDTO response =
                paymentInstrumentService.createCreditCard(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/credit-cards/{id}/update")
    public ResponseEntity<CreditCardDetailsDTO> updateCreditCard(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCreditCardRequestDTO dto
    ) {
        CreditCardDetailsDTO response =
                paymentInstrumentService.updateCreditCard(dto, id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterStatus(
            @PathVariable UUID id
    ) {
        paymentInstrumentService.alterStatusInstrument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodDetailsDTO>> findAll(
    ) {
        return ResponseEntity.ok(paymentInstrumentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodDetailsDTO> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(paymentInstrumentService.findById(id));
    }
}

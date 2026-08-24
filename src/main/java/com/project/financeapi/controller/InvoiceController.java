package com.project.financeapi.controller;

import com.project.financeapi.dto.Installments.InstallmentResponseDTO;
import com.project.financeapi.dto.invoice.CreateInvoiceRequestDTO;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.service.InvoiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoice")
@Tag(name = "Faturas", description = "Gerenciamento e processamento de faturas")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/create")
    public ResponseEntity<InvoiceResponseDTO> create(
            @Valid @RequestBody CreateInvoiceRequestDTO dto) {
        InvoiceResponseDTO invoice = invoiceService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping()
    public ResponseEntity<List<InvoiceResponseDTO>> findAll(
    ) {
        List<InvoiceResponseDTO> invoice = invoiceService.findAll();

        return ResponseEntity.status(HttpStatus.OK).body(invoice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> findById(
            @Valid @PathVariable UUID id
    ) {

        InvoiceResponseDTO invoice = invoiceService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(invoice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable UUID id
    ) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/installment/{installmentId}")
    public ResponseEntity<Void> deleteInstallment(
            @PathVariable UUID installmentId
    ) {
        invoiceService.deleteInstallment(installmentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/installment/{installmentId}")
    public ResponseEntity<InstallmentResponseDTO> updateInstallment(
            @PathVariable UUID installmentId,
            @Valid @RequestBody com.project.financeapi.dto.Installments.UpdateInstallmentRequestDTO dto
    ) {
        return ResponseEntity.ok(invoiceService.updateInstallment(installmentId, dto));
    }
}

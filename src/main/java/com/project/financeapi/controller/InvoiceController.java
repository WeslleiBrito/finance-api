package com.project.financeapi.controller;

import com.project.financeapi.dto.invoice.CreateInvoiceRequestDTO;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/create")
    public ResponseEntity<InvoiceResponseDTO> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateInvoiceRequestDTO dto) {
        InvoiceResponseDTO invoice = invoiceService.create(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping()
    public ResponseEntity<List<InvoiceResponseDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token) {
        List<InvoiceResponseDTO> invoice = invoiceService.findAll(token);

        return ResponseEntity.status(HttpStatus.OK).body(invoice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> findById(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable String id) {

        InvoiceResponseDTO invoice = invoiceService.findById(token, id);

        return ResponseEntity.status(HttpStatus.OK).body(invoice);
    }
}

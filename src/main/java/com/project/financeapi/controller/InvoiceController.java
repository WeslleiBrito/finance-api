package com.project.financeapi.controller;

import com.project.financeapi.dto.document.CreateDocumentRequestDTO;
import com.project.financeapi.dto.document.DocumentResponseDTO;
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
    public ResponseEntity<DocumentResponseDTO> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateDocumentRequestDTO dto) {
        DocumentResponseDTO invoice = invoiceService.create(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping()
    public ResponseEntity<List<DocumentResponseDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token) {
        List<DocumentResponseDTO> invoice = invoiceService.findAll(token);

        return ResponseEntity.status(HttpStatus.OK).body(invoice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> findById(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable String id) {

        DocumentResponseDTO invoice = invoiceService.findById(token, id);

        return ResponseEntity.status(HttpStatus.OK).body(invoice);
    }
}

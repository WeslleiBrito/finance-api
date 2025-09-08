package com.project.financeapi.controller;

import com.project.financeapi.dto.document.CreateDocumentRequestDTO;
import com.project.financeapi.entity.Document;
import com.project.financeapi.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/create")
    public ResponseEntity<Document> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CreateDocumentRequestDTO dto)
    {
        Document document = documentService.create(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }
}

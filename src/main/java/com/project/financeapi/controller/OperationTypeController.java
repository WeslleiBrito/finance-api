package com.project.financeapi.controller;

import com.project.financeapi.dto.OperationType.OperationTypeRequestCreateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeRequestUpdateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.ResponseDefaultDTO;
import com.project.financeapi.service.OperationTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operation-type")
@Tag(name = "Tipos de Operação", description = "Gerenciamento das categorias e tipos de operações financeiras")
public class OperationTypeController {

    private final OperationTypeService operationTypeService;

    public OperationTypeController(OperationTypeService operationTypeService) {
        this.operationTypeService = operationTypeService;
    }

    @PostMapping("/create")
    public ResponseEntity<OperationTypeResponseDTO> create(
            @Valid @RequestBody OperationTypeRequestCreateDTO dto
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(operationTypeService.create(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDefaultDTO> update(
            @Valid @PathVariable UUID id,
            @Valid@RequestBody OperationTypeRequestUpdateDTO dto
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(operationTypeService.update(id, dto));
    }

    @PatchMapping("/update-status/{id}")
    public ResponseEntity<HttpStatus> updateStatus(
            @Valid @PathVariable UUID id
    ) {
        operationTypeService.updateStatusOperationType(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<OperationTypeResponseDTO>> findAll(
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(operationTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationTypeResponseDTO> findById(
            @Valid @PathVariable UUID id
    ){
        return ResponseEntity.status(HttpStatus.OK).body(operationTypeService.findById(id));
    }

    @GetMapping("/operation-status/{status}")
    public ResponseEntity<List<OperationTypeResponseDTO>> findAllOperationStatus(
            @Valid @PathVariable boolean isEnabled
    ){
        return ResponseEntity.status(HttpStatus.OK).body(operationTypeService.findAllOperationStatus(isEnabled));
    }
}

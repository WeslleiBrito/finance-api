package com.project.financeapi.controller;

import com.project.financeapi.dto.OperationType.OperationTypeRequestCreateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeRequestUpdateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.ResponseDefaultDTO;
import com.project.financeapi.service.OperationTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operation-type")
public class OperationTypeController {

    private final OperationTypeService operationTypeService;

    public OperationTypeController(OperationTypeService operationTypeService) {
        this.operationTypeService = operationTypeService;
    }

    @PostMapping("/create")
    public ResponseEntity<OperationTypeResponseDTO> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody OperationTypeRequestCreateDTO dto
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(operationTypeService.create(token, dto));
    }

    @GetMapping
    public ResponseEntity<List<OperationTypeResponseDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(operationTypeService.findAll(token));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDefaultDTO> update(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable String id,
            @Valid@RequestBody OperationTypeRequestUpdateDTO dto
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(operationTypeService.update(token, id, dto));
    }
}

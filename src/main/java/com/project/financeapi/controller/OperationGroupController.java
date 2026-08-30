package com.project.financeapi.controller;

import com.project.financeapi.dto.operationGroup_.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup_.OperationGroupResponseDTO;
import com.project.financeapi.dto.operationGroup_.UpdateRequestOperationGroup;
import com.project.financeapi.enumSystem.StatusEntity;
import com.project.financeapi.service.OperationGroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operation-group")
@Tag(name = "Grupos de Operação", description = "Gerenciamento de agrupamentos para operações financeiras")
public class OperationGroupController {

    private final OperationGroupService operationGroupService;

    public OperationGroupController(OperationGroupService operationGroupService) {
        this.operationGroupService = operationGroupService;
    }

    @PostMapping("/create")
    public ResponseEntity<OperationGroupResponseDTO> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody OperationGroupCreateRequestDTO dto
        )
    {
        OperationGroupResponseDTO operationGroup = operationGroupService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(operationGroup);
    }

    @GetMapping
    public ResponseEntity<List<OperationGroupResponseDTO>> findAll(
    ){
        List<OperationGroupResponseDTO> operationGroups = operationGroupService.findAll();

        return ResponseEntity.status(HttpStatus.OK).body(operationGroups);
    }

    @GetMapping("/operation-staus/{status}")
    public ResponseEntity<List<OperationGroupResponseDTO>> findActive(
            @Valid @PathVariable StatusEntity status
            ){
        List<OperationGroupResponseDTO> operationGroups = operationGroupService.findAllOperationStatus(status);

        return ResponseEntity.status(HttpStatus.OK).body(operationGroups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationGroupResponseDTO> findById(
            @Valid @PathVariable UUID id
    ) {
        OperationGroupResponseDTO operationGroup = operationGroupService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(operationGroup);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<OperationGroupResponseDTO> update(
            @Valid @RequestBody UpdateRequestOperationGroup dto,
            @Valid @PathVariable UUID id
    ){

        OperationGroupResponseDTO response = operationGroupService.update(id, dto);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }

    @PatchMapping("/update-status/{id}")
    public ResponseEntity<HttpStatus> updateStatusOperationGroup(
            @Valid @PathVariable UUID id
    ){

        operationGroupService.updateStatusOperationGroup(id);

        return ResponseEntity.noContent().build();

    }
}

package com.project.financeapi.controller;

import com.project.financeapi.dto.ResponseDefaultDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.operationGroup.UpdateRequestOperationGroup;
import com.project.financeapi.enums.OperationStatus;
import com.project.financeapi.service.OperationGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operation-group")
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
        OperationGroupResponseDTO operationGroup = operationGroupService.create(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(operationGroup);
    }

    @GetMapping
    public ResponseEntity<List<OperationGroupResponseDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token
    ){
        List<OperationGroupResponseDTO> operationGroups = operationGroupService.findAll(token);

        return ResponseEntity.status(HttpStatus.OK).body(operationGroups);
    }

    @GetMapping("/operation-staus/{status}")
    public ResponseEntity<List<OperationGroupResponseDTO>> findActive(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable OperationStatus status
            ){
        List<OperationGroupResponseDTO> operationGroups = operationGroupService.findAllOperationStatus(token, status);

        return ResponseEntity.status(HttpStatus.OK).body(operationGroups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationGroupResponseDTO> findById(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable UUID id
    ) {
        OperationGroupResponseDTO operationGroup = operationGroupService.findById(token, id);

        return ResponseEntity.status(HttpStatus.OK).body(operationGroup);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDefaultDTO> update(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateRequestOperationGroup dto,
            @Valid @PathVariable UUID id
    ){

        ResponseDefaultDTO response = operationGroupService.update(token, id, dto);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }

    @PatchMapping("/update-status/{id}")
    public ResponseEntity<HttpStatus> updateStatusOperationGroup(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable UUID id
    ){

        operationGroupService.updateStatusOperationGroup(token, id);

        return ResponseEntity.noContent().build();

    }
}

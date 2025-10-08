package com.project.financeapi.controller;

import com.project.financeapi.dto.operationGroup.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.service.OperationGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

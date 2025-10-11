package com.project.financeapi.controller;

import com.project.financeapi.dto.ResponseDefault;
import com.project.financeapi.dto.operationGroup.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.operationGroup.UpdateRequestOperationGroup;
import com.project.financeapi.dto.operationGroup.UpdateStatusRequestOperationGroupDTO;
import com.project.financeapi.service.OperationGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/active")
    public ResponseEntity<List<OperationGroupResponseDTO>> findActive(
            @RequestHeader("X-Auth-Token") String token
    ){
        List<OperationGroupResponseDTO> operationGroups = operationGroupService.findActive(token);

        return ResponseEntity.status(HttpStatus.OK).body(operationGroups);
    }

    @GetMapping("/inactivated")
    public ResponseEntity<List<OperationGroupResponseDTO>> findInactivated(
            @RequestHeader("X-Auth-Token") String token
    ){
        List<OperationGroupResponseDTO> operationGroups = operationGroupService.findInactivated(token);

        return ResponseEntity.status(HttpStatus.OK).body(operationGroups);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDefault> update(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateRequestOperationGroup dto,
            @PathVariable String id
    ){

        ResponseDefault response = operationGroupService.update(token, id, dto);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<ResponseDefault> updateStatusOperationGroup(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody UpdateStatusRequestOperationGroupDTO dto,
            @PathVariable String id
    ){

        ResponseDefault response = operationGroupService.updateStatusOperationGroup(token, id, dto);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }
}

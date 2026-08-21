package com.project.financeapi.controller;

import com.project.financeapi.dto.person.*;
import com.project.financeapi.service.PersonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/person")
@Tag(name = "Pessoas", description = "Endpoints para cadastro e consulta de pessoas físicas e jurídicas")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping("/create/physical")
    public ResponseEntity<PersonResponseDTO> createPhysical(
            @Valid @RequestBody PersonCreatePhysicalRequestDTO dto
    )
    {
        PersonResponseDTO person = personService.createPhysicalPerson(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @PostMapping("/create/legal")
    public ResponseEntity<PersonResponseDTO> createLegal(
            @Valid @RequestBody PersonCreateLegalRequestDTO dto
    )
    {
        PersonResponseDTO person = personService.createLegalPerson(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> findAll(
    ){
        List<PersonResponseDTO> persons = personService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(persons);
    }

    // ==========================================
    // NOVOS ENDPOINTS: ATUALIZAÇÃO (PUT)
    // ==========================================

    @PutMapping("/update/physical/{id}")
    public ResponseEntity<PersonResponseDTO> updatePhysical(
            @PathVariable UUID id,
            @Valid @RequestBody PersonCreatePhysicalRequestDTO dto
    ) {
        // Chama o método que criamos no PersonService
        PersonResponseDTO person = personService.updatePhysicalPerson(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(person);
    }

    @PutMapping("/update/legal/{id}")
    public ResponseEntity<PersonResponseDTO> updateLegal(
            @PathVariable UUID id,
            @Valid @RequestBody PersonCreateLegalRequestDTO dto
    ) {
        // Chama o método que criamos no PersonService
        PersonResponseDTO person = personService.updateLegalPerson(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(person);
    }
}
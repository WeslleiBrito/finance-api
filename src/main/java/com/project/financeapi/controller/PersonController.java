package com.project.financeapi.controller;

import com.project.financeapi.dto.person.*;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping("/create/physical")
    public ResponseEntity<PersonResponseDTO> createPhysical(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody PersonCreatePhysicalRequestDTO dto
    )
    {
        PersonResponseDTO person = personService.createPhysicalPerson(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @PostMapping("/create/legal")
    public ResponseEntity<PersonResponseDTO> createLegal(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody PersonCreateLegalRequestDTO dto
    )
    {
        PersonResponseDTO person = personService.createLegalPerson(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> findAll(
            @RequestHeader("X-Auth-Token") String token
    ){
        List<PersonResponseDTO> persons = personService.findAll(token);

        return ResponseEntity.status(HttpStatus.OK).body(persons);
    }
}

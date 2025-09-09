package com.project.financeapi.controller;

import com.project.financeapi.dto.person.PersonCreateRequestDTO;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping("/create")
    public ResponseEntity<PersonBase> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody PersonCreateRequestDTO dto
            )
    {
        PersonBase person = personService.create(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }
}

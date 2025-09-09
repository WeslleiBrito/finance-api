package com.project.financeapi.controller;

import com.project.financeapi.dto.ResponseDefault;
import com.project.financeapi.dto.phone.PhoneCreateRequestDTO;
import com.project.financeapi.service.PhoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/phone")
public class PhoneController {
    private final PhoneService phoneService;

    public PhoneController(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDefault> create(@RequestHeader("X-Auth-Token") String token,
                                                  @Valid @RequestBody PhoneCreateRequestDTO dto) {
        phoneService.create(token, dto.idPerson(), dto.phoneList().phones());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseDefault(
                        (dto.phoneList().phones().size() > 1) ?
                                "Os telefones/celulares foram criados com sucesso" :
                                "O telefone/celular foi criado com sucesso"
                )
        );
    }

}
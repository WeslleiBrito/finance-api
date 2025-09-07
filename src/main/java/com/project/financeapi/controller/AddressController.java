package com.project.financeapi.controller;

import com.project.financeapi.dto.ResponseDefault;
import com.project.financeapi.dto.address.AddressCreateRequestDTO;
import com.project.financeapi.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDefault> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody AddressCreateRequestDTO dto
    ){
     addressService.create(token, dto.idPerson(), dto.addressesList().addresses());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseDefault(
                        (dto.addressesList().addresses().size() > 1) ?
                                "Os endereços foram criados com sucesso" : "O endereço foi criado com sucesso")
        );
    }
}

package com.project.financeapi.controller;

import com.project.financeapi.dto.card.CardBrandCreateRequestDTO;
import com.project.financeapi.dto.card.CardBrandResponseDTO;
import com.project.financeapi.dto.card.CardBrandUpdateRequestDTO;
import com.project.financeapi.service.CardBrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card-brand")
@RequiredArgsConstructor
public class CardBrandController {

    private final CardBrandService cardBrandService;

    @PostMapping
    public ResponseEntity<CardBrandResponseDTO> create(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CardBrandCreateRequestDTO dto
    ) {

        CardBrandResponseDTO cardBrand = cardBrandService.create(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(cardBrand);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardBrandResponseDTO> update(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody CardBrandUpdateRequestDTO dto,
            @PathVariable String id
    ) {

        CardBrandResponseDTO cardBrand = cardBrandService.update(token, dto, id);

        return ResponseEntity.status(HttpStatus.OK).body(cardBrand);
    }

    @GetMapping
    public ResponseEntity<List<CardBrandResponseDTO>> getAll(
            @RequestHeader("X-Auth-Token") String token
    ) {

        List<CardBrandResponseDTO> cardBrands = cardBrandService.getAll(token);

        return ResponseEntity.status(HttpStatus.OK).body(cardBrands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardBrandResponseDTO> findById(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable String id
    ) {
        CardBrandResponseDTO cardBrand = cardBrandService.getById(token, id);

        return ResponseEntity.status(HttpStatus.OK).body(cardBrand);
    }
}

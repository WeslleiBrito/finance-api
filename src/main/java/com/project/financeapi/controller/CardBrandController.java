package com.project.financeapi.controller;

import com.project.financeapi.dto.card.cardBrand.CardBrandCreateRequestDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandUpdateRequestDTO;
import com.project.financeapi.enums.CardBrandStatus;
import com.project.financeapi.service.CardBrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
            @Valid @PathVariable UUID id
    ) {

        CardBrandResponseDTO cardBrand = cardBrandService.update(token, dto, id);

        return ResponseEntity.status(HttpStatus.OK).body(cardBrand);
    }

    @PatchMapping("/update-status/{id}")
    public ResponseEntity<HttpStatus> updateStatus(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable UUID id
    ){
        cardBrandService.updateStatus(token, id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CardBrandResponseDTO>> getAll(
            @RequestHeader("X-Auth-Token") String token
    ) {

        List<CardBrandResponseDTO> cardBrands = cardBrandService.findAll(token);

        return ResponseEntity.status(HttpStatus.OK).body(cardBrands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardBrandResponseDTO> findById(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable UUID id
    ) {
        CardBrandResponseDTO cardBrand = cardBrandService.findById(token, id);

        return ResponseEntity.status(HttpStatus.OK).body(cardBrand);
    }

    @GetMapping("/card-brand-status/{status}")
    public ResponseEntity<List<CardBrandResponseDTO>> findAllStatus(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @PathVariable CardBrandStatus status
    ){
      return ResponseEntity.status(HttpStatus.OK).body(cardBrandService.findAllCardBrandStatus(token, status));
    }
}

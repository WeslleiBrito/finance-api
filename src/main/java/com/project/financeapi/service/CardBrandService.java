package com.project.financeapi.service;

import com.project.financeapi.dto.card.cardBrand.CardBrandCreateRequestDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandUpdateRequestDTO;
import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.CardBrandStatus;
import com.project.financeapi.exception.AccessBlockedException;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.CardBrandRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardBrandService {

    private final CardBrandRepository cardBrandRepository;
    private final UserContextService userContextService;

    @Transactional
    public CardBrandResponseDTO create(String token, @NotNull CardBrandCreateRequestDTO dto) {

        User user = userContextService.getAuthenticatedUser();

        if(cardBrandRepository.nameExitsByCreatedBy(user, dto.name())){
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma bandeira de cartão com este nome.");
        }

        CardBrand cardBrand = cardBrandRepository.save(new CardBrand(dto.name(), user));

        return cardBrand.toResponse();

    }

    @Transactional
    public CardBrandResponseDTO update(@NotNull CardBrandUpdateRequestDTO dto, UUID id) {

        User user = userContextService.getAuthenticatedUser();

        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada.")
                );

        if(dto.name() != null){

            if(!cardBrand.getName().equalsIgnoreCase(dto.name())){

                if(cardBrandRepository.nameExitsByCreatedBy(user, dto.name())){
                    throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma bandeira de cartão com este nome.");
                }
            }

            cardBrand.setName(dto.name());
        }

        return cardBrandRepository.save(cardBrand).toResponse();
    }

    @Transactional
    public void updateStatus(UUID id){

        User user = userContextService.getAuthenticatedUser();

        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado.")
        );

        if(cardBrand.isGlobal()){
            throw new AccessBlockedException("Você não tem permissão para mudar o status desta bandeira de cartão.");
        }

        cardBrand.setStatus(cardBrand.getStatus().toggle());

        cardBrandRepository.save(cardBrand);

    }

    public List<CardBrandResponseDTO> findAll() {

        User user = userContextService.getAuthenticatedUser();

        List<CardBrand> cardBrands = cardBrandRepository.findAllByCreatedBy(user.getId());

        return cardBrands.stream().map(CardBrand::toResponse).toList();
    }

    public CardBrandResponseDTO findById(UUID id) {

        User user = userContextService.getAuthenticatedUser();

        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada."));

        return cardBrand.toResponse();
    }

    public List<CardBrandResponseDTO> findAllCardBrandStatus(CardBrandStatus cardBrandStatus) {

        User user = userContextService.getAuthenticatedUser();

        return cardBrandRepository.findAllByUserCardBrandStatus(user, cardBrandStatus).stream()
                .map(CardBrand::toResponse).toList();
    }


}

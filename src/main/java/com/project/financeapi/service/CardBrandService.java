package com.project.financeapi.service;

import com.project.financeapi.dto.card.cardBrand.CardBrandCreateRequestDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandUpdateRequestDTO;
import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.DeactivatedCardBrand;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.CardBrandStatus;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.CardBrandRepository;
import com.project.financeapi.repository.DeactivatedCardBrandRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardBrandService {

    private final CardBrandRepository cardBrandRepository;
    private final UserContextService userContextService;
    private final DeactivatedCardBrandRepository deactivatedRepo;

    @Transactional
    public CardBrandResponseDTO create(String token, @NotNull CardBrandCreateRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        if(cardBrandRepository.nameExitsByCreatedBy(user, dto.name())){
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma bandeira de cartão com este nome.");
        }

        CardBrand cardBrand = cardBrandRepository.save(new CardBrand(dto.name(), user));
        return cardBrand.toResponse(CardBrandStatus.ACTIVE);
    }

    @Transactional
    public CardBrandResponseDTO update(@NotNull CardBrandUpdateRequestDTO dto, UUID id) {
        User user = userContextService.getAuthenticatedUser();
        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada."));

        // 🌟 TRAVA DE SEGURANÇA: Não edita bandeira do sistema
        if(cardBrand.isGlobal()){
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar uma bandeira global.");
        }

        if(dto.name() != null && !cardBrand.getName().equalsIgnoreCase(dto.name())){
            if(cardBrandRepository.nameExitsByCreatedBy(user, dto.name())){
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma bandeira de cartão com este nome.");
            }
            cardBrand.setName(dto.name());
        }

        cardBrand = cardBrandRepository.save(cardBrand);

        boolean isDeactivated = deactivatedRepo.findByUserIdAndCardBrandId(user.getId(), id).isPresent();
        return cardBrand.toResponse(isDeactivated ? CardBrandStatus.INACTIVE : CardBrandStatus.ACTIVE);
    }

    @Transactional
    public void updateStatus(UUID id){
        User user = userContextService.getAuthenticatedUser();
        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira não encontrada."));

        // 🌟 LÓGICA DE BLACKLIST
        Optional<DeactivatedCardBrand> deactivated = deactivatedRepo.findByUserIdAndCardBrandId(user.getId(), id);
        if (deactivated.isPresent()) {
            deactivatedRepo.delete(deactivated.get());
        } else {
            deactivatedRepo.save(new DeactivatedCardBrand(user, cardBrand));
        }
    }

    public List<CardBrandResponseDTO> findAll() {
        User user = userContextService.getAuthenticatedUser();
        List<CardBrand> cardBrands = cardBrandRepository.findAllByCreatedBy(user.getId());
        Set<UUID> deactivatedIds = deactivatedRepo.findDeactivatedBrandIdsByUserId(user.getId());

        return cardBrands.stream().map(brand -> {
            CardBrandStatus status = deactivatedIds.contains(brand.getId()) ? CardBrandStatus.INACTIVE : CardBrandStatus.ACTIVE;
            return brand.toResponse(status);
        }).toList();
    }

    public CardBrandResponseDTO findById(UUID id) {
        User user = userContextService.getAuthenticatedUser();
        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada."));

        boolean isDeactivated = deactivatedRepo.findByUserIdAndCardBrandId(user.getId(), id).isPresent();
        return cardBrand.toResponse(isDeactivated ? CardBrandStatus.INACTIVE : CardBrandStatus.ACTIVE);
    }

    public List<CardBrandResponseDTO> findAllCardBrandStatus(CardBrandStatus cardBrandStatus) {
        User user = userContextService.getAuthenticatedUser();
        List<CardBrand> cardBrands = cardBrandRepository.findAllByCreatedBy(user.getId());
        Set<UUID> deactivatedIds = deactivatedRepo.findDeactivatedBrandIdsByUserId(user.getId());

        return cardBrands.stream()
                .filter(brand -> {
                    CardBrandStatus currentStatus = deactivatedIds.contains(brand.getId()) ? CardBrandStatus.INACTIVE : CardBrandStatus.ACTIVE;
                    return currentStatus == cardBrandStatus;
                })
                .map(brand -> brand.toResponse(cardBrandStatus))
                .toList();
    }
}
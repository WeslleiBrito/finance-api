package com.project.financeapi.service;

import com.project.financeapi.dto.card.cardBrand.CardBrandCreateRequestDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandUpdateRequestDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.CardBrandStatus;
import com.project.financeapi.exception.AccessBlockedException;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.CardBrandRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
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
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public CardBrandResponseDTO create(String token, @NotNull CardBrandCreateRequestDTO dto) {

        User user = getUser(token);

        if(cardBrandRepository.nameExitsByCreatedBy(user, dto.name())){
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma bandeira de cartão com este nome.");
        }

        CardBrand cardBrand = cardBrandRepository.save(new CardBrand(dto.name(), user));

        return cardBrand.toResponse();

    }

    @Transactional
    public CardBrandResponseDTO update(String token, @NotNull CardBrandUpdateRequestDTO dto, UUID id) {

        User user = getUser(token);

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
    public void updateStatus(String token, UUID id){

        User user = getUser(token);

        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado.")
        );

        if(cardBrand.isGlobal()){
            throw new AccessBlockedException("Você não tem permissão para mudar o status desta bandeira de cartão.");
        }

        cardBrand.setStatus(cardBrand.getStatus().toggle());

        cardBrandRepository.save(cardBrand);

    }

    public List<CardBrandResponseDTO> findAll(String token) {

        User user = getUser(token);

        List<CardBrand> cardBrands = cardBrandRepository.findAllByCreatedBy(user.getId());

        return cardBrands.stream().map(CardBrand::toResponse).toList();
    }

    public CardBrandResponseDTO findById(String token, UUID id) {

        User user = getUser(token);

        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada."));

        return cardBrand.toResponse();
    }

    public List<CardBrandResponseDTO> findAllCardBrandStatus(String token, CardBrandStatus cardBrandStatus) {

        User user = getUser(token);

        return cardBrandRepository.findAllByUserCardBrandStatus(user, cardBrandStatus).stream()
                .map(CardBrand::toResponse).toList();
    }

    private User getUser(String token) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        return userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));
    }
}

package com.project.financeapi.service;

import com.project.financeapi.dto.card.cardBrand.CardBrandCreateRequestDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandUpdateRequestDTO;
import com.project.financeapi.dto.user.UserResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.CardBrandRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
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

    public CardBrandResponseDTO create(String token, CardBrandCreateRequestDTO dto){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CardBrand cardBrand = cardBrandRepository.save(new CardBrand(dto.name(), user));

        return new CardBrandResponseDTO(
                cardBrand.getId(),
                cardBrand.getName(),
                cardBrand.getStatus(),
                cardBrand.isGlobal(),
                cardBrand.getCreatedAt()
        );

    }

    public CardBrandResponseDTO update(String token, CardBrandUpdateRequestDTO dto, UUID id){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user, id)
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada.")
                );

        if(dto.name() != null){
            cardBrand.setName(dto.name());
        }

        if(dto.cardBrandStatus() != null){
            cardBrand.setStatus(dto.cardBrandStatus());
        }

        cardBrandRepository.save(cardBrand);

        return new CardBrandResponseDTO(
                cardBrand.getId(),
                cardBrand.getName(),
                cardBrand.getStatus(),
                cardBrand.isGlobal(),
                cardBrand.getCreatedAt()
        );
    }


    public List<CardBrandResponseDTO> getAll(String token){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        List<CardBrand> cardBrands = cardBrandRepository.findAllByCreatedBy(user);

        return cardBrands.stream().map(
                cardBrand -> new CardBrandResponseDTO(
                        cardBrand.getId(),
                        cardBrand.getName(),
                        cardBrand.getStatus(),
                        cardBrand.isGlobal(),
                        cardBrand.getCreatedAt()
                )
        ).toList();
    }

    public CardBrandResponseDTO getById(String token, UUID id) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));


        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user, id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada."));


        return new CardBrandResponseDTO(
                cardBrand.getId(),
                cardBrand.getName(),
                cardBrand.getStatus(),
                cardBrand.isGlobal(),
                cardBrand.getCreatedAt()
        );
    }
}

package com.project.financeapi.service;

import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.CreditCard;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final UserContextService userContextService;

    // 🌟 Anotação obrigatória para permitir o carregamento das parcelas vinculadas
    @Transactional(readOnly = true)
    public List<CreditCardDetailsDTO> getAll() {
        User user = userContextService.getAuthenticatedUser();

        return creditCardRepository.findAllByCreatedBy_Id(user.getId())
                .stream()
                .map(CreditCard::toDTO)
                .toList();
    }

    // 🌟 Anotação obrigatória para permitir o carregamento das parcelas vinculadas
    @Transactional(readOnly = true)
    public CreditCardDetailsDTO getById(UUID id) {
        User user = userContextService.getAuthenticatedUser();

        CreditCard creditCard = creditCardRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cartão não encontrado."));

        return creditCard.toDTO();
    }
}
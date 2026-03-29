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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardService {


    private final CreditCardRepository creditCardRepository;
    private final UserContextService userContextService;
    private static final Logger logger = LoggerFactory.getLogger(CreditCardService.class);


    public List<CreditCardDetailsDTO> getAll(String token){

        try {

            User user = userContextService.getAuthenticatedUser();

            List<CreditCard> creditCards = creditCardRepository.findAllByCreatedBy_Id(user.getId());

            return creditCards.stream().map(
                    CreditCard::toDTO
            ).toList();

        } catch (Exception e) {
            logger.error(">>> ERRO FATAL no método getAll: ", e);
            throw e;
        }
    }

    public CreditCardDetailsDTO getById(String token, UUID id) {

        User user = userContextService.getAuthenticatedUser();

        CreditCard creditCard = creditCardRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cartão não encontrado."));

        return creditCard.toDTO();
    }

}

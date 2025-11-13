package com.project.financeapi.service;

import com.project.financeapi.dto.card.creditCard.CreditCardCreateRequestDTO;
import com.project.financeapi.dto.card.creditCard.CreditCardUpdateRequestDTO;
import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.CreditCard;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import com.project.financeapi.util.JwtUtil;
import com.project.financeapi.util.mapper.CreditCardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditCardService {


    private final CreditCardRepository creditCardRepository;
    private final UserRepository userRepository;
    private final CardBrandRepository cardBrandRepository;
    private final BankRepository bankRepository;
    private final JwtUtil jwtUtil;
    private final CreditCardMapper creditCardMapper;


    public CreditCardDetailsDTO create(
            String token,
            CreditCardCreateRequestDTO dto
    ) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), dto.cardBrand())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada."));


        Bank bank = null;

        if(dto.bank() != null){

            bank = bankRepository.findByCreatedByAndId(user, dto.bank())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        }

        CreditCard creditCard = creditCardRepository.save(
                new CreditCard(
                        dto.name(),
                        user,
                        dto.creditLimit(),
                        dto.closingDay(),
                        dto.dueDay(),
                        dto.expirationDate(),
                        cardBrand,
                        bank,
                        dto.revolvingInterest(),
                        dto.fine()
                )
        );

        return creditCard.toDTO();
    }


    public CreditCardDetailsDTO update(String token, CreditCardUpdateRequestDTO dto, UUID id) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CreditCard creditCard = creditCardRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cartão não encontrado."));

        creditCardMapper.updateCreditCardDTO(dto, creditCard);

        creditCardRepository.save(creditCard);

        return creditCard.toDTO();
    }


    public List<CreditCardDetailsDTO> getAll(String token){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        List<CreditCard> creditCards = creditCardRepository.findAllByCreatedBy_Id(user.getId());

        return creditCards.stream().map(
                CreditCard::toDTO
        ).toList();

    }

    public CreditCardDetailsDTO getById(String token, UUID id) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CreditCard creditCard = creditCardRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cartão não encontrado."));

        return creditCard.toDTO();
    }
}

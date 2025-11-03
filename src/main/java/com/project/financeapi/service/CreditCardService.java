package com.project.financeapi.service;

import com.project.financeapi.dto.bank.BankResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.card.creditCard.CreditCardCreateRequestDTO;
import com.project.financeapi.dto.card.creditCard.CreditCardResponseDTO;
import com.project.financeapi.dto.card.creditCard.CreditCardUpdateRequestDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.payment.CreditCard;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.BankRepository;
import com.project.financeapi.repository.CardBrandRepository;
import com.project.financeapi.repository.CardRepository;
import com.project.financeapi.repository.UserRepository;
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

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardBrandRepository cardBrandRepository;
    private final BankRepository bankRepository;
    private final JwtUtil jwtUtil;
    private final CreditCardMapper creditCardMapper;

    private CreditCardResponseDTO responseDTO (CreditCard creditCard) {
        return new CreditCardResponseDTO(
                creditCard.getId(),
                creditCard.getName(),
                creditCard.getCreditLimit(),
                creditCard.getClosingDay(),
                new CardBrandResponseDTO(
                        creditCard.getCardBrand().getId(),
                        creditCard.getCardBrand().getName(),
                        creditCard.getCardBrand().getStatus(),
                        creditCard.getCardBrand().isGlobal(),
                        creditCard.getCardBrand().getCreatedAt()
                ),
                new BankResponseDTO(
                        creditCard.getBank().getId(),
                        creditCard.getBank().getName(),
                        creditCard.getBank().getCode(),
                        creditCard.getBank().getStatus()
                ),
                creditCard.getAvailableLimit(),
                creditCard.getRevolvingInterest(),
                creditCard.getFine()
        );
    }

    public CreditCardResponseDTO create(
            String token,
            CreditCardCreateRequestDTO dto
    ) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user, dto.cardBrand())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Bandeira de cartão não encontrada."));


        Bank bank = null;

        if(dto.bank() != null){

            bank = bankRepository.findByCreatedByAndId(user, dto.bank())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        }

        CreditCard creditCard = cardRepository.save(
                new CreditCard(
                        dto.name(),
                        user,
                        dto.creditLimit(),
                        dto.closingDay(),
                        dto.dueDay(),
                        cardBrand,
                        bank,
                        dto.revolvingInterest(),
                        dto.fine()
                )
        );

        return responseDTO(creditCard);
    }


    public CreditCardResponseDTO update(String token, CreditCardUpdateRequestDTO dto, UUID id) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CreditCard creditCard = cardRepository.findCreditCardByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cartão não encontrado."));

        creditCardMapper.updateCreditCardDTO(dto, creditCard);

        cardRepository.save(creditCard);

        return responseDTO(creditCard);
    }


    public List<CreditCardResponseDTO> getAll(String token){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        List<CreditCard> creditCards = cardRepository.findAllCreditCardByCreatedBy(user.getId());

        return creditCards.stream().map(this::responseDTO).toList();

    }

    public CreditCardResponseDTO getById(String token, UUID id) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CreditCard creditCard = cardRepository.findCreditCardByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cartão não encontrado."));

        return responseDTO(creditCard);
    }
}

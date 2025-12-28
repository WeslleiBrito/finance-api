package com.project.financeapi.service;

import com.project.financeapi.dto.card.creditCard.CreditCardCreateRequestDTO;
import com.project.financeapi.dto.card.creditCard.UpdateCreditCardRequestDTO;
import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.dto.payment.PaymentMethodDetailsDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.CreditCard;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import com.project.financeapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentInstrumentService {
    private final PaymentInstrumentRepository paymentInstrumentRepository;
    private final CreditCardRepository creditCardRepository;
    private final UserRepository userRepository;
    private final CardBrandRepository cardBrandRepository;
    private final BankRepository bankRepository;
    private final JwtUtil jwtUtil;


    public CreditCardDetailsDTO createCreditCard(String token, @NotNull CreditCardCreateRequestDTO dto) {

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

        if(paymentInstrumentRepository.findByName(user.getId(), dto.name()).isPresent()){
            throw  new BusinessException(HttpStatus.CONFLICT, "Já existe um instrumento de pagamento com esse nome.");
        }

        if(dto.revolvingInterest() != null && dto.revolvingInterest().longValue() < 0.00){
            throw new BusinessException(HttpStatus.BAD_REQUEST, "O valor do juros rotativo precisa ser maior que 0.00");
        }

        if(dto.fine() != null && dto.fine().longValue() < 0.00){
            throw new BusinessException(HttpStatus.BAD_REQUEST, "O valor da multa precisa ser maior que 0.00.");
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

    public CreditCardDetailsDTO updateCreditCard(String token, UpdateCreditCardRequestDTO dto, UUID id) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        CreditCard creditCard = creditCardRepository.findByCreatedByAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "Cartão não encontrado")
        );

        if(dto.name() != null){
            if(paymentInstrumentRepository.findByName(user.getId(), dto.name()).isEmpty()){
                creditCard.setName(dto.name());
            }
        }

        if(dto.creditLimit() != null){
            if(dto.creditLimit().longValue() >= 0.01){
                creditCard.setCreditLimit(dto.creditLimit());
            }else{
                throw new BusinessException(HttpStatus.BAD_REQUEST, "O valor mínimo do limite é 0,01 centavo.");
            }
        }
        if(dto.closingDay() != null){
            if(dto.closingDay() >= 1 && dto.closingDay() <= 31){
                creditCard.setClosingDay(dto.closingDay());
            }else{
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "A data de fechamento precisa está entre os dias 1 a 31."
                );
            }
        }

        if(dto.dueDay() != null){
            if(dto.dueDay() >= 1 && dto.dueDay() <= 31){
                creditCard.setDueDay(dto.dueDay());
            }else{
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "A data de vencimento precisa está entre os dias 1 a 31."
                );
            }
        }

        if(dto.cardBrandId() != null){

            CardBrand cardBrand = cardBrandRepository.findByCreatedByAndId(user.getId(), dto.cardBrandId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                            "Bandeira de cartão não encontrada.")
                    );

            creditCard.setCardBrand(cardBrand);
        }

        if(dto.bankId() != null){

            Bank bank = bankRepository.findByCreatedByAndId(user, dto.bankId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

            creditCard.setBank(bank);

        }

        if(dto.revolvingInterest() != null){
            if(dto.revolvingInterest().longValue() > 0.00){
                creditCard.setRevolvingInterest(dto.revolvingInterest());
            }else{
                throw new BusinessException(HttpStatus.BAD_REQUEST, "O valor do juros rotativo precisa ser maior que 0.00");
            }
        }

        if(dto.fine() != null){
            if(dto.fine().longValue() > 0.00){
                creditCard.setFine(dto.fine());
            }else{
                throw new BusinessException(HttpStatus.BAD_REQUEST, "O valor da multa precisa ser maior que 0.00.");
            }
        }

        if(dto.expirationDate() != null){
            creditCard.setExpirationDate(dto.expirationDate());
        }

        return creditCardRepository.save(creditCard).toDTO();
    }

    public void alterStatusInstrument(String token, UUID id) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        PaymentInstrumentBase paymentInstrument = paymentInstrumentRepository.findByIdAndUser(id, user.getId())
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "Instrumento de pagamento/compra não encontrado.")
                );

        if(paymentInstrument.getCreatedBy() == null){
            throw new BusinessException(HttpStatus.FORBIDDEN, "Não é permitido editar este instrumento de pagamento/compra.");
        }

        paymentInstrument.setStatus(paymentInstrument.getStatus().toggle());

        paymentInstrumentRepository.save(paymentInstrument);
    }

    public List<PaymentMethodDetailsDTO> findAll(String token) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        return paymentInstrumentRepository.findByCreatedAll(user.getId())
                .stream()
                .map(PaymentInstrumentBase::toDTO)
                .toList();
    }

    public PaymentMethodDetailsDTO findById(String token, UUID id) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        PaymentInstrumentBase instrument = paymentInstrumentRepository.findByIdAndUser(id, user.getId())
                .orElseThrow(() ->
                        new BusinessException(HttpStatus.NOT_FOUND, "Instrumento de pagamento/compra não encontrado.")
                );

        return instrument.toDTO();
    }

}

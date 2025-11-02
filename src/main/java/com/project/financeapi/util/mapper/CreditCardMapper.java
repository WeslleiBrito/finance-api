package com.project.financeapi.util.mapper;

import com.project.financeapi.dto.card.creditCard.CreditCardUpdateRequestDTO;
import com.project.financeapi.entity.payment.CreditCard;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

public interface CreditCardMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCreditCardDTO(CreditCardUpdateRequestDTO dto, @MappingTarget CreditCard creditCard);
}

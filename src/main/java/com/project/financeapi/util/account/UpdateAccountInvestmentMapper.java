package com.project.financeapi.util.account;

import com.project.financeapi.dto.card.creditCard.CreditCardUpdateRequestDTO;
import com.project.financeapi.entity.CreditCard;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UpdateAccountInvestmentMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCreditCardDTO(CreditCardUpdateRequestDTO dto, @MappingTarget CreditCard creditCard);

}

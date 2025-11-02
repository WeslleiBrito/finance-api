package com.project.financeapi.util.mapper;

import com.project.financeapi.dto.bank.BankUpdateRequestDTO;
import com.project.financeapi.entity.Bank;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "string")
public interface BankMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBankDTO(BankUpdateRequestDTO dto, @MappingTarget Bank bank);
}

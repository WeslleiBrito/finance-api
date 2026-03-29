package com.project.financeapi.service;

import com.project.financeapi.dto.bank.*;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.enumSystem.BankStatus;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;


    public BankResponseDTO getById(UUID id){

        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        return bank.toResponse();
    }

    public List<BankResponseDTO> findAll() {

        return bankRepository.findAll().stream().map(Bank::toResponse).toList();
    }

    public List<BankResponseDTO> findAllBankStatus(BankStatus bankStatus) {

        return bankRepository.findAllBankStatus(bankStatus).stream()
                .map(Bank::toResponse).toList();
    }


}

package com.project.financeapi.service;

import com.project.financeapi.dto.bank.*;
import com.project.financeapi.dto.bank.BankUpdateRequestDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.BankStatus;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.BankRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.mapper.BankMapperUpdate;
import com.project.financeapi.util.JwtUtil;
import com.project.financeapi.util.mapper.bank.BankMapperResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BankMapperUpdate bankMapperUpdate;


    @Transactional
    public BankResponseDTO update(String token, BankUpdateRequestDTO dto, UUID id) {

        User user = getUser(token);

        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        bankMapperUpdate.updateBankDTO(dto, bank);

        if(!bank.getName().equalsIgnoreCase(dto.name())) {

            if (bankRepository.nameExitsById(dto.name())) {

                throw new BusinessException(HttpStatus.CONFLICT, "Já existe um banco com este nome.");
            }
        }

        bank = bankRepository.save(bank);

        return BankMapperResponse.toResponse(bank);
    }

    public BankResponseDTO getById(String token, UUID id){

        User user = getUser(token);

        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        return bank.toResponse();
    }

    public List<BankResponseDTO> findAll(String token) {

        User user = getUser(token);

        return bankRepository.findAll().stream().map(Bank::toResponse).toList();
    }

    public List<BankResponseDTO> findAllBankStatus(String token, BankStatus bankStatus) {

        User user = getUser(token);

        return bankRepository.findAllBankStatus(bankStatus).stream()
                .map(Bank::toResponse).toList();
    }


    private User getUser(String token) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        return userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));
    }

}

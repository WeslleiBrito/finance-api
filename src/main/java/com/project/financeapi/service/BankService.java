package com.project.financeapi.service;

import com.project.financeapi.dto.bank.BankCreateRequestDTO;
import com.project.financeapi.dto.bank.BankResponseDTO;
import com.project.financeapi.dto.bank.BankUpdateRequestDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.BankStatus;
import com.project.financeapi.exception.AccessBlockedException;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.BankRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.mapper.BankMapper;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
    private final BankMapper bankMapper;

    @Transactional
    public BankResponseDTO create(String token, @NotNull BankCreateRequestDTO dto) {

        User user = getUser(token);

        if(bankRepository.nameExitsByCreatedById(user, dto.name())){
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe um banco com este nome.");
        }

        Bank bank = bankRepository.save(new Bank(dto.name(), dto.code(), user));

        return bank.toResponse();
    }

    @Transactional
    public BankResponseDTO update(String token, BankUpdateRequestDTO dto, UUID id) {

        User user = getUser(token);

        Bank bank = bankRepository.findByCreatedByAndId(user, id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        bankMapper.updateBankDTO(dto, bank);

        if(!bank.getName().equalsIgnoreCase(dto.name())) {

            if (bankRepository.nameExitsByCreatedById(user, dto.name())) {

                throw new BusinessException(HttpStatus.CONFLICT, "Já existe um banco com este nome.");
            }
        }

        bank = bankRepository.save(bank);

        return bank.toResponse();
    }

    @Transactional
    public void updateStatus(String token, UUID id){

        User user = getUser(token);

        Bank bank = bankRepository.findByCreatedByAndId(user, id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado.")
        );

        if(bank.getIsGlobal()){
            throw new AccessBlockedException("Você não tem permissão para mudar o status deste banco.");
        }

        bank.setStatus(bank.getStatus().toggle());

        bankRepository.save(bank);

    }

    public BankResponseDTO getById(String token, UUID id){

        User user = getUser(token);

        Bank bank = bankRepository.findByCreatedByAndId(user, id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        return bank.toResponse();
    }

    public List<BankResponseDTO> findAll(String token) {

        User user = getUser(token);

        return bankRepository.findAllByCreatedBy(user).stream().map(Bank::toResponse).toList();
    }

    public List<BankResponseDTO> findAllBankStatus(String token, BankStatus bankStatus) {

        User user = getUser(token);

        return bankRepository.findAllByUserBankStatus(user, bankStatus).stream()
                .map(Bank::toResponse).toList();
    }

    private User getUser(String token) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        return userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));
    }
}

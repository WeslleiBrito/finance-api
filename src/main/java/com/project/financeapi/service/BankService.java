package com.project.financeapi.service;

import com.project.financeapi.dto.bank.BankCreateRequestDTO;
import com.project.financeapi.dto.bank.BankResponseDTO;
import com.project.financeapi.dto.bank.BankUpdateRequestDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.BankRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.mapper.BankMapper;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class BankService {

    private BankRepository bankRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BankMapper bankMapper;

    @Transactional
    public BankResponseDTO create(String token, BankCreateRequestDTO dto) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Bank bank = bankRepository.save(new Bank(dto.name(), dto.code(), user));

        return new BankResponseDTO(
                bank.getId(),
                bank.getName(),
                bank.getCode(),
                bank.getStatus()
        );
    }

    @Transactional
    public BankResponseDTO update(String token, BankUpdateRequestDTO dto, UUID id)
    {
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));


        Bank bank = bankRepository.findByCreatedByAndId(user, id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        bankMapper.updateBankDTO(dto, bank);

        bankRepository.save(bank);

        return new BankResponseDTO(
                bank.getId(),
                bank.getName(),
                bank.getCode(),
                bank.getStatus()
        );
    }

    public List<BankResponseDTO> getAll(String token) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));


        List<Bank> banks = bankRepository.findAllByCreatedBy(user);

        return banks.stream().map(
                bank ->  new BankResponseDTO(
                        bank.getId(),
                        bank.getName(),
                        bank.getCode(),
                        bank.getStatus()
        )).toList();
    }

    public BankResponseDTO getById(String token, UUID id){
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));


        Bank bank = bankRepository.findByCreatedByAndId(user, id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado."));

        return new BankResponseDTO(
                bank.getId(),
                bank.getName(),
                bank.getCode(),
                bank.getStatus()
        );
    }
}

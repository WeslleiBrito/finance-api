package com.project.financeapi.service;

import com.project.financeapi.dto.account.CreateAccountRequestDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.account.ResponseDeactivateAccountDTO;
import com.project.financeapi.dto.account.UpdateAccountRequestDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.*;
import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.exception.AccessBlockedException;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.AccountRepository;
import com.project.financeapi.repository.TransactionRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          JwtUtil jwtUtil
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AccountBase create(String token, CreateAccountRequestDTO dto) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        AccountBase account = null;

        switch (dto.type()) {
            case CHECKING -> account = new CheckingAccount(user, dto.initialValue());
            case SAVINGS -> account = new SavingsAccount(user, dto.initialValue());
            case PAYMENT -> account = new PaymentAccount(user, dto.initialValue());
            case WALLET -> account = new WalletAccount(user, dto.initialValue());
            case INVESTMENT -> account = new InvestmentAccount(user, dto.initialValue());
        }

        account.setName(dto.name());

        return accountRepository.save(account);

    }

    @Transactional
    public AccountBase update(String token, String id, UpdateAccountRequestDTO dto) {

        JwtPayload userToken = jwtUtil.extractPayload(token);

        userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        AccountBase account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));


        if (!account.getAccountHolder().getId().equals(userToken.id())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar esta conta");
        }

        if (dto.name() != null && !dto.name().isBlank()) {
            account.setName(dto.name());
        }

        if (dto.type() != null) {
            account.setType(dto.type());
        }

        if (dto.status() != null) {
            account.setStatus(dto.status());
        }

        return accountRepository.save(account);
    }

    public List<ResponseAccountDTO> findAll(String token) {

        JwtPayload userToken = jwtUtil.extractPayload(token);

        User user = userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        List<AccountBase> accounts = accountRepository.findByAccountHolder(user);

        return accounts.stream().filter(accountBase -> accountBase.getStatus() == AccountStatus.ACTIVE)
                .map(account -> new ResponseAccountDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getStatus()
        )).collect(Collectors.toList());

    }

    public ResponseAccountDTO findById(String token, String id) {

        JwtPayload userToken = jwtUtil.extractPayload(token);

        User user = userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        AccountBase account = accountRepository.findByAccountHolderAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        if (!account.getAccountHolder().equals(user)) {
            throw new RuntimeException("Verifique o id informado, conta não localizada.");
        }

        return new ResponseAccountDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getStatus()
        );
    }

    public ResponseDeactivateAccountDTO deactivateAccount(String token, String id) {

        JwtPayload userToken = jwtUtil.extractPayload(token);

        User user = userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        AccountBase account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        if (!account.getAccountHolder().equals(user)) {
            throw new AccessBlockedException("Você não tem permissão para inativar esta conta.");
        }

        account.setStatus(AccountStatus.INACTIVATED);

        accountRepository.save(account);

        return new ResponseDeactivateAccountDTO(
                account.getId(),
                "A conta " + account.getName() + " foi desativada com sucesso."
        );
    }

}

package com.project.financeapi.service;

import com.project.financeapi.dto.ResponseValidateDTO;
import com.project.financeapi.dto.account.create.*;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.account.ResponseDeactivateAccountDTO;
import com.project.financeapi.dto.account.UpdateAccountRequestDTO;
import com.project.financeapi.dto.account.response.*;
import com.project.financeapi.dto.account.response.CreateCheckingAccountResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.account.*;
import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.exception.AccessBlockedException;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.AccountRepository;
import com.project.financeapi.repository.BankRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BankRepository bankRepository;


    @Transactional
    public CreateCheckingAccountResponseDTO create(String token, CreateCheckingAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        CheckingAccount account = new CheckingAccount(
                validate.user(), dto.baseAccount().name(), dto.baseAccount().initialValue(),
                validate.bank(), dto.overdraftLimit()
        );

        account = accountRepository.save(account);

        return new CreateCheckingAccountResponseDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getStatus(),
                account.getOverdraftLimit()
        );

    }

    @Transactional
    public CreateInvestmentAccountResponseDTO create(String token, CreateInvestmentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        InvestmentAccount account = new InvestmentAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.riskLevel()
        );

        account = accountRepository.save(account);

        return new CreateInvestmentAccountResponseDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getStatus(),
                account.getRiskLevel()
        );

    }

    @Transactional
    public CreateSavingsAccountResponseDTO create(String token, CreateSavingsAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        SavingsAccount account = new SavingsAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.interestRate()
        );

        account = accountRepository.save(account);

        return new CreateSavingsAccountResponseDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getStatus(),
                account.getInterestRate()
        );

    }

    @Transactional
    public CreatePaymentAccountResponseDTO create(String token, CreatePaymentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        PaymentAccount account = new PaymentAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.provider()
        );

        account = accountRepository.save(account);

        return new CreatePaymentAccountResponseDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getStatus(),
                account.getProvider()
        );

    }

    @Transactional
    public CreateWalletAccountResponseDTO create(String token, CreateWalletAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        WalletAccount account = new WalletAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank()
        );

        account = accountRepository.save(account);

        return new CreateWalletAccountResponseDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getStatus()
        );

    }
  

    @Transactional
    public AccountBase update(String token, UUID id, UpdateAccountRequestDTO dto) {

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

    public ResponseAccountDTO findById(String token, UUID id) {

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

    public ResponseDeactivateAccountDTO deactivateAccount(String token, UUID id) {

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
                account.getId().toString(),
                "A conta " + account.getName() + " foi desativada com sucesso."
        );
    }


    private ResponseValidateDTO validate(String token, UUID bankId) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Bank bank = null;

        if(bankId != null){
            bank = bankRepository.findByCreatedByAndId(user, bankId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado"));
        }

        return new ResponseValidateDTO(
                user,
                bank
        );
    }

}

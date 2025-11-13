package com.project.financeapi.service;

import com.project.financeapi.dto.ResponseValidateDTO;
import com.project.financeapi.dto.account.AccountResponseDTO;
import com.project.financeapi.dto.account.create.*;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.account.ResponseDeactivateAccountDTO;
import com.project.financeapi.dto.account.UpdateAccountRequestDTO;
import com.project.financeapi.dto.account.response.*;
import com.project.financeapi.dto.account.response.CreateCheckingAccountResponseDTO;
import com.project.financeapi.dto.account.update.*;
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


        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public CreateInvestmentAccountResponseDTO create(String token, CreateInvestmentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        InvestmentAccount account = new InvestmentAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.riskLevel()
        );


        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public CreateSavingsAccountResponseDTO create(String token, CreateSavingsAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        SavingsAccount account = new SavingsAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.interestRate()
        );

        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public CreatePaymentAccountResponseDTO create(String token, CreatePaymentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        PaymentAccount account = new PaymentAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.provider()
        );


        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public CreateWalletAccountResponseDTO create(String token, CreateWalletAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.baseAccount().bankId());

        WalletAccount account = new WalletAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank()
        );


        return accountRepository.save(account).toDTO();

    }
  

    @Transactional
    public AccountResponseDTO update(String token, UUID id, UpdateCheckingAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));


        if(validate.bank() != null){
            account.setBank(validate.bank());
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    @Transactional
    public AccountResponseDTO update(String token, UUID id, UpdateInvestmentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));


        if(validate.bank() != null){
            account.setBank(validate.bank());
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    @Transactional
    public AccountResponseDTO update(String token, UUID id, UpdatePaymentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));


        if(validate.bank() != null){
            account.setBank(validate.bank());
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    @Transactional
    public AccountResponseDTO update(String token, UUID id, UpdateSavingsAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));


        if(validate.bank() != null){
            account.setBank(validate.bank());
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    @Transactional
    public AccountResponseDTO update(String token, UUID id, UpdateWalletAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(token, dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));


        if(validate.bank() != null){
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Esse tipo de conta não aceita banco.");
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    public List<AccountResponseDTO> findAll(String token) {

        JwtPayload userToken = jwtUtil.extractPayload(token);

        User user = userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        List<AccountBase> accounts = accountRepository.findByAccountHolder(user);


        return accounts.stream().filter(accountBase -> accountBase.getStatus() == AccountStatus.ACTIVE)
                .map(AccountBase::toDTO).toList();

    }

    public AccountResponseDTO findById(String token, UUID id) {

        JwtPayload userToken = jwtUtil.extractPayload(token);

        User user = userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        AccountBase account = accountRepository.findByAccountHolderAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        if (!account.getAccountHolder().equals(user)) {
            throw new RuntimeException("Verifique o id informado, conta não localizada.");
        }

        return account.toDTO();
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

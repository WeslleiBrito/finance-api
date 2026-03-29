package com.project.financeapi.service;

import com.project.financeapi.dto.ResponseValidateDTO;
import com.project.financeapi.dto.account.AccountResponseDTO;
import com.project.financeapi.dto.account.create.*;
import com.project.financeapi.dto.account.response.*;
import com.project.financeapi.dto.account.response.CreateCheckingAccountResponseDTO;
import com.project.financeapi.dto.account.update.*;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.account.*;
import com.project.financeapi.enumSystem.AccountStatus;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.AccountRepository;
import com.project.financeapi.repository.BankRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;
    private final UserContextService userContextService;


    @Transactional
    public CreateCheckingAccountResponseDTO create(@NotNull CreateCheckingAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.baseAccount().bankId());

        this.validateCreateName(validate.user().getId(), dto.baseAccount().name());

        CheckingAccount account = new CheckingAccount(
                validate.user(), dto.baseAccount().name(), dto.baseAccount().initialValue(),
                validate.bank(), dto.overdraftLimit()
        );


        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public CreateInvestmentAccountResponseDTO create(@NotNull CreateInvestmentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.baseAccount().bankId());

        this.validateCreateName(validate.user().getId(), dto.baseAccount().name());

        InvestmentAccount account = new InvestmentAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.riskLevel()
        );


        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public CreateSavingsAccountResponseDTO create(@NotNull CreateSavingsAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.baseAccount().bankId());

        this.validateCreateName(validate.user().getId(), dto.baseAccount().name());

        SavingsAccount account = new SavingsAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.interestRate()
        );

        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public CreatePaymentAccountResponseDTO create(@NotNull CreatePaymentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.baseAccount().bankId());

        this.validateCreateName(validate.user().getId(), dto.baseAccount().name());

        PaymentAccount account = new PaymentAccount(
                validate.user(), dto.baseAccount().name(),
                dto.baseAccount().initialValue(),
                validate.bank(), dto.provider()
        );


        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public CreateWalletAccountResponseDTO create(@NotNull CreateWalletAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(null);

        this.validateCreateName(validate.user().getId(), dto.name());

        WalletAccount account = new WalletAccount(
                validate.user(),
                dto.name(),
                dto.initialValue() != null ? dto.initialValue(): BigDecimal.ZERO
        );

        return accountRepository.save(account).toDTO();

    }

    @Transactional
    public AccountResponseDTO update(UUID id, @NotNull UpdateCheckingAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

        this.validateUpdateName(validate.user().getId(), account.getName(), dto.name());

        if(validate.bank() != null){
            account.setBank(validate.bank());
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    @Transactional
    public AccountResponseDTO update(UUID id, @NotNull UpdateInvestmentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

        this.validateUpdateName(validate.user().getId(), account.getName(), dto.name());

        if(validate.bank() != null){
            account.setBank(validate.bank());
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    @Transactional
    public AccountResponseDTO update(UUID id, @NotNull UpdatePaymentAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

        this.validateUpdateName(validate.user().getId(), account.getName(), dto.name());

        if(validate.bank() != null){
            account.setBank(validate.bank());
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    @Transactional
    public AccountResponseDTO update(UUID id, @NotNull UpdateSavingsAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

        this.validateUpdateName(validate.user().getId(), account.getName(), dto.name());

        if(validate.bank() != null){
            account.setBank(validate.bank());
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    @Transactional
    public AccountResponseDTO update(UUID id, @NotNull UpdateWalletAccountRequestDTO dto) {

        ResponseValidateDTO validate = validate(dto.bankId());

        AccountBase account = accountRepository.findByAccountHolderAndId(validate.user(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

        this.validateUpdateName(validate.user().getId(), account.getName(), dto.name());

        if(validate.bank() != null){
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Esse tipo de conta não aceita banco.");
        }

        account.updateFrom(dto);

        return accountRepository.save(account).toDTO();
    }

    public List<AccountResponseDTO> findAll() {

        User user = userContextService.getAuthenticatedUser();

        List<AccountBase> accounts = accountRepository.findByAccountHolder(user);


        return accounts.stream().filter(accountBase -> accountBase.getStatus() == AccountStatus.ACTIVE)
                .map(AccountBase::toDTO).toList();

    }

    public AccountResponseDTO findById(UUID id) {

        User user = userContextService.getAuthenticatedUser();

        AccountBase account = accountRepository.findByAccountHolderAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        if (!account.getAccountHolder().equals(user)) {
            throw new RuntimeException("Verifique o id informado, conta não localizada.");
        }

        return account.toDTO();
    }

    public void updateStatus(UUID id) {

        User user = userContextService.getAuthenticatedUser();

        AccountBase account = accountRepository.findByAccountHolderAndId(user, id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "Conta não encontrada.")
        );

        account.setStatus(account.getStatus().toggle());

        accountRepository.save(account);

    }


    @NotNull
    private ResponseValidateDTO validate(UUID bankId) {

        User user = userContextService.getAuthenticatedUser();

        Bank bank = null;

        if(bankId != null){
            bank = bankRepository.findById(bankId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Banco não encontrado"));
        }


        return new ResponseValidateDTO(
                user,
                bank
        );
    }

    private void validateCreateName(String userId, String name){

        if(accountRepository.nameExitsByAccountHolderId(name, userId)){
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma conta com este nome.");
        }

    }

    private void validateUpdateName(String userId, String currentName, String newName){

        if(accountRepository.nameExitsByAccountHolderId(newName, userId)){

            if(!currentName.equalsIgnoreCase(newName)){
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma conta com este nome.");
            }
        }

    }

}

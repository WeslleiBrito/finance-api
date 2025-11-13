package com.project.financeapi.entity.account;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.dto.account.update.UpdateCheckingAccountRequestDTO;
import com.project.financeapi.dto.account.response.CreateCheckingAccountResponseDTO;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "checking_account")
@Getter
@Setter
public class CheckingAccount extends AccountBase {
    @Column(name ="overdraft_limit")
    private BigDecimal overdraftLimit = BigDecimal.ZERO; // limite do cheque especial

    public CheckingAccount(User accountHolder, String name, BigDecimal initialValue, Bank bank, BigDecimal overdraftLimit) {
        super(AccountType.CHECKING, accountHolder, name, initialValue, bank);
        this.overdraftLimit = overdraftLimit;
    }

    public CheckingAccount() {
    }
    @Override
    public void updateFrom(AccountUpdateDTO dto) {

        if (!(dto instanceof UpdateCheckingAccountRequestDTO checkingDto)) {
            throw new IllegalArgumentException("DTO incompatível com conta corrente");
        }

        if(checkingDto.overdraftLimit() != null){
            this.overdraftLimit = checkingDto.overdraftLimit();
        }
    }
    @Override
    public CreateCheckingAccountResponseDTO toDTO(){

        return new CreateCheckingAccountResponseDTO(
                this.getId(),
                this.getName(),
                this.getType(),
                this.getBalance(),
                this.getStatus(),
                this.getOverdraftLimit()
        );
    }


}

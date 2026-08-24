package com.project.financeapi.entity.account;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.dto.account.response.CreateSavingsAccountResponseDTO;
import com.project.financeapi.dto.account.update.UpdateSavingsAccountRequestDTO;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "savings_account")
@Getter
@Setter
public class SavingsAccount extends AccountBase {

    @Column(name = "interest_rate")
    private BigDecimal interestRate = BigDecimal.valueOf(0.005);

    public SavingsAccount(User accountHolder, String name, BigDecimal initialValue, Bank bank, BigDecimal interestRate) {
        super(AccountType.SAVINGS, accountHolder, name, initialValue, bank);
        this.interestRate = interestRate;
    }

    public SavingsAccount() {
    }

    @Override
    public CreateSavingsAccountResponseDTO toDTO(){
        return new CreateSavingsAccountResponseDTO(
                this.getId(),
                this.getName(),
                this.getType(),
                this.getBalance(),
                this.getStatus(),
                this.getInterestRate(),
                this.getTransactions().stream().map(
                        Transaction::toResponse
                ).toList()
        );
    }

    @Override
    public void updateFrom(AccountUpdateDTO dto) {

        if (!(dto instanceof UpdateSavingsAccountRequestDTO paymentDto)) {
            throw new IllegalArgumentException("DTO incompatível com conta corrente");
        }

        if(paymentDto.interestRate() != null) this.interestRate = paymentDto.interestRate();

    }
}

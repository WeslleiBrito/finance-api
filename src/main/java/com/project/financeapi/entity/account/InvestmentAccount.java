package com.project.financeapi.entity.account;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.dto.account.response.CreateInvestmentAccountResponseDTO;
import com.project.financeapi.dto.account.update.UpdateInvestmentAccountRequestDTO;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "investment_account")
@Getter
@Setter
public class InvestmentAccount extends AccountBase {

    private BigDecimal riskLevel;

    public InvestmentAccount(User accountHolder, String name, BigDecimal initialValue, Bank bank, BigDecimal riskLevel) {
        super(AccountType.INVESTMENT, accountHolder, name, initialValue, bank);
        this.riskLevel = riskLevel;
    }

    public InvestmentAccount() {
    }

    @Override
    public CreateInvestmentAccountResponseDTO toDTO(){
        return new CreateInvestmentAccountResponseDTO(
                this.getId(),
                this.getName(),
                this.getType(),
                this.getBalance(),
                this.getStatus(),
                this.riskLevel,
                this.getTransactions().stream().map(
                        Transaction::toResponse
                ).toList()
        );
    }

    @Override
    public void updateFrom(AccountUpdateDTO dto) {
        if (!(dto instanceof UpdateInvestmentAccountRequestDTO investmentDto)) {
            throw new IllegalArgumentException("DTO incompatível com conta corrente");
        }

        if(investmentDto.riskLevel() != null){
            this.riskLevel = investmentDto.riskLevel();
        }
    }
}

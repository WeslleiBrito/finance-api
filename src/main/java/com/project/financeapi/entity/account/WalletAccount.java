package com.project.financeapi.entity.account;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.dto.account.response.CreateInvestmentAccountResponseDTO;
import com.project.financeapi.dto.account.response.CreateWalletAccountResponseDTO;
import com.project.financeapi.dto.account.update.UpdateSavingsAccountRequestDTO;
import com.project.financeapi.dto.account.update.UpdateWalletAccountRequestDTO;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet_account")
public class WalletAccount extends AccountBase {

    public WalletAccount(User accountHolder, String name, BigDecimal initialValue, Bank bank) {
        super(AccountType.WALLET, accountHolder, name, initialValue, bank);
    }

    public WalletAccount() {
    }

    @Override
    public CreateWalletAccountResponseDTO toDTO(){
        return new CreateWalletAccountResponseDTO(
                this.getId(),
                this.getName(),
                this.getType(),
                this.getBalance(),
                this.getStatus()
        );
    }


    @Override
    public void updateFrom(AccountUpdateDTO dto) {

        if (!(dto instanceof UpdateWalletAccountRequestDTO paymentDto)) {
            throw new IllegalArgumentException("DTO incompatível com conta corrente");
        }

    }
}

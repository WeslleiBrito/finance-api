package com.project.financeapi.entity.account;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.dto.account.response.CreatePaymentAccountResponseDTO;
import com.project.financeapi.dto.account.update.UpdatePaymentAccountRequestDTO;
import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_account")
@Getter
@Setter
public class PaymentAccount extends AccountBase {
    private String provider;

    public PaymentAccount(User accountHolder, String name, BigDecimal initialValue, Bank bank, String provider) {
        super(AccountType.PAYMENT, accountHolder, name, initialValue, bank);
        this.provider = provider;
    }

    public PaymentAccount() {
    }

    @Override
    public CreatePaymentAccountResponseDTO toDTO(){
        return new CreatePaymentAccountResponseDTO(
                this.getId(),
                this.getName(),
                this.getType(),
                this.getBalance(),
                this.getStatus(),
                this.provider
        );
    }

    @Override
    public void updateFrom(AccountUpdateDTO dto) {

        if (!(dto instanceof UpdatePaymentAccountRequestDTO paymentDto)) {
            throw new IllegalArgumentException("DTO incompatível com conta corrente");
        }

        if(paymentDto.provider() != null) this.provider = paymentDto.provider();
    }
}

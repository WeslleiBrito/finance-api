package com.project.financeapi.entity.payment;

import com.project.financeapi.entity.Installment;
import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.CardBase;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;



@Data
@Entity
@Table(name = "credit_card")
public class CreditCard extends CardBase {

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name="closing_day", nullable = false)
    private Integer closingDay;

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column(name = "available_limit", nullable = false)
    private BigDecimal availableLimit;


    public CreditCard(String name, User createdBy, BigDecimal creditLimit, Integer closingDay, Integer dueDay) {
        super(name, createdBy);
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
    }


}

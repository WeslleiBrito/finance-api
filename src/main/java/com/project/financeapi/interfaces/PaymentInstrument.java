package com.project.financeapi.interfaces;

import com.project.financeapi.dto.payment.PaymentMethodDetailsDTO;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.enums.InstrumentNature;
import java.util.List;

public interface PaymentInstrument {

    /**
     * Nome do instrumento (ex: "Cartão Nubank", "Conta Itaú", "Carteira PIX").
     */
    String getName();

    /**
     * Natureza do instrumento (ex: PURCHASE, PAYMENT).
     */
    InstrumentNature getInstrumentNature();

    /**
     * Lista de parcelas associadas a este instrumento.
     * Cada parcela pode conter suas próprias transações.
     */
    List<Installment> getInstallments();

    /**
     * Retorna o saldo total consolidado de todas as parcelas (ainda abertas).
     */
    default java.math.BigDecimal getTotalBalance() {
        return getInstallments().stream()
                .map(Installment::getRemainingBalance)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * Retorna o total já pago (ou recebido) em todas as parcelas.
     */
    default java.math.BigDecimal getTotalPaid() {
        return getInstallments().stream()
                .map(Installment::getTotalPaid)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }


}

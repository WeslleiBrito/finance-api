package com.project.financeapi.entity;

import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.dto.payment.SimplePaymentInstrumentDetailsDTO;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.DiscriminatorValue;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "simple_payment_instrument") // Nome da nova tabela
@PrimaryKeyJoinColumn(name = "id")         // Faz o vínculo com a tabela pai
@DiscriminatorValue("SIMPLE")              // Identificador interno do Hibernate
public class SimplePaymentInstrument extends PaymentInstrumentBase {

    // Construtor utilitário para facilitar a criação via código (Data Seeder)
    public SimplePaymentInstrument() {
    }

    /**
     * Implementação do toDTO.
     * Retorna um DTO básico, já que não tem limite nem fatura para mostrar.
     */
    @Override
    public SimplePaymentInstrumentDetailsDTO toDTO() {
        // Supondo que você tenha um DTO genérico que retorna apenas ID, Nome e Tipo.
        // Se não tiver, pode retornar o próprio objeto ou criar um SimplePaymentDTO.
        return new SimplePaymentInstrumentDetailsDTO(
                this.getId(),
                this.getPaymentType(),
                this.getIsGlobal(),
                this.getCreatedAt(),
                this.getInstrumentNature()
        );
    }

    @Override
    public List<Installment> getInstallments() {
        return Collections.emptyList();
        // ou return new ArrayList<>(); se precisar alterar a lista depois
    }

    /**
     * Implementação do process.
     * Para Pix/Dinheiro, geralmente não há lógica de parcelamento complexa.
     * Apenas retornamos as parcelas como estão (geralmente é à vista).
     */
    @Override
    public List<InstallmentDTO> process(List<InstallmentDTO> installments) {
        // Se for Pix/Dinheiro, não muda a data de vencimento nem valida limite.
        // Apenas repassa.
        return installments;
    }
}
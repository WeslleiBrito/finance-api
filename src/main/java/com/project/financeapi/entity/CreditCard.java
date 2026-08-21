package com.project.financeapi.entity;

import com.project.financeapi.dto.Installments.InstallmentDTO;
import com.project.financeapi.dto.bank.*;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.InstrumentNature;
import com.project.financeapi.enumSystem.PaymentType;
import com.project.financeapi.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Data
@Entity
@Table(name = "credit_card")
@DiscriminatorValue("CREDIT_CARD")
public class CreditCard extends PaymentInstrumentBase {

    @Column(name = "card_holder_name", nullable = false, length = 60)
    private String cardHolderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_brand_id")
    private CardBrand cardBrand;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name="closing_day", nullable = false)
    private Integer closingDay;

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column(name = "revolving_interest", nullable = false)
    private BigDecimal revolvingInterest;

    @Column(name = "fine", nullable = false)
    private BigDecimal fine;

    @OneToMany(mappedBy = "paymentInstrument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    @Override
    public List<Installment> getInstallments() {
        return installments;
    }
    public CreditCard(){}

    public CreditCard(
            String name,
            User createdBy,
            BigDecimal creditLimit,
            Integer closingDay,
            Integer dueDay,
            LocalDate expirationDate,
            CardBrand cardBrand,
            Bank bank,
            BigDecimal revolvingInterest,
            BigDecimal fine
    ) {
        super(name, createdBy, InstrumentNature.PURCHASE, PaymentType.CREDIT_CARD);
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.revolvingInterest = revolvingInterest != null ? revolvingInterest : BigDecimal.ZERO;
        this.expirationDate = expirationDate;
        this.cardBrand = cardBrand;
        this.bank = bank;
        this.fine = fine != null ? fine : BigDecimal.ZERO;
        this.cardHolderName = name;
    }

    /**
     * Calcula o limite disponível com base nas parcelas em aberto.
     */
    @Transient
    public BigDecimal getAvailableLimit() {
        if (getInstallments() == null || getInstallments().isEmpty()) {
            return creditLimit;
        }

        BigDecimal totalEmAberto = getInstallments().stream()
                .map(Installment::getRemainingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return creditLimit.subtract(totalEmAberto.max(BigDecimal.ZERO));
    }


    @Override
    public CreditCardDetailsDTO toDTO(){

        return new CreditCardDetailsDTO(
                this.getId(),
                this.getPaymentType(),
                this.getCreatedAt(),
                this.getInstrumentNature(),
                this.getExpirationDate(),
                this.cardHolderName,
                this.closingDay,
                this.dueDay,
                this.creditLimit,
                this.getAvailableLimit(),
                this.revolvingInterest,
                this.fine,
                this.getStatus(),
                new CardBrandResponseDTO(
                        this.getCardBrand().getId(),
                        this.getCardBrand().getName(),
                        this.getCardBrand().getStatus(),
                        this.getCardBrand().isGlobal(),
                        this.getCardBrand().getCreatedAt()
                ),
                new BankResponseDTO(
                        this.getBank().getId(),
                        this.getBank().getName(),
                        this.getBank().getCode(),
                        this.getBank().getStatus()
                )
        );
    }

    @Override
    public List<InstallmentDTO> process(List<InstallmentDTO> installments) {

        if (installments == null || installments.isEmpty()) {
            return installments;
        }

        // Ordena por número da parcela
        List<InstallmentDTO> ordered = installments.stream()
                .sorted(Comparator.comparing(InstallmentDTO::parcelNumber))
                .toList();

        BigDecimal totalPurchaseAmount = ordered.stream()
                .map(InstallmentDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Valida limite
        if (getAvailableLimit().compareTo(totalPurchaseAmount) < 0) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Limite insuficiente no cartão de crédito."
            );
        }

        LocalDate today = LocalDate.now();

        // Determina se entra na fatura atual ou próxima
        boolean afterClosing =
                today.getDayOfMonth() > this.closingDay;

        LocalDate firstDueDate = afterClosing
                ? calculateDueDate(today.plusMonths(1))
                : calculateDueDate(today);

        List<InstallmentDTO> processed = new ArrayList<>();

        for (int i = 0; i < ordered.size(); i++) {

            InstallmentDTO dto = ordered.get(i);

            LocalDate dueDate = firstDueDate.plusMonths(i);

            processed.add(new InstallmentDTO(
                    dto.amount(),
                    dto.parcelNumber(),
                    dueDate,
                    dto.instrument()
            ));
        }

        return processed;
    }

    /**
     * Calcula a data de vencimento baseada no dueDay
     */
    private LocalDate calculateDueDate(LocalDate baseDate) {

        int day = Math.min(this.dueDay, baseDate.lengthOfMonth());

        return LocalDate.of(
                baseDate.getYear(),
                baseDate.getMonth(),
                day
        );
    }

}

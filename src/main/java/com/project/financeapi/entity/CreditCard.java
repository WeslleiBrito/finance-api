package com.project.financeapi.entity;

import com.project.financeapi.dto.Installments.InstallmentDTO;
import com.project.financeapi.dto.bank.*;
import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.InstrumentNature;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.PaymentType;
import com.project.financeapi.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
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

    // 🌟 NOVA COLUNA: Limite já comprometido
    @Column(name = "used_limit", nullable = false)
    private BigDecimal usedLimit = BigDecimal.ZERO;

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
        this.usedLimit = BigDecimal.ZERO;
    }

    /**
     * 🌟 AGORA COM O(1) DE COMPLEXIDADE: Cálculo instantâneo do limite
     */
    @Transient
    public BigDecimal getAvailableLimit() {
        return this.creditLimit.subtract(this.usedLimit);
    }

    /**
     * Consome o limite do cartão (Ex: ao fazer uma nova compra)
     */
    public void consumeLimit(BigDecimal amount) {
        if (amount == null) return;
        if (getAvailableLimit().compareTo(amount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Limite insuficiente no cartão de crédito.");
        }
        this.usedLimit = this.usedLimit.add(amount);
    }

    /**
     * Libera o limite do cartão (Ex: ao pagar a fatura ou estornar uma compra)
     */
    public void freeUpLimit(BigDecimal amount) {
        if (amount == null) return;
        // Subtrai o valor e garante que o limite usado nunca fique negativo
        this.usedLimit = this.usedLimit.subtract(amount).max(BigDecimal.ZERO);
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
                this.getCardBrand().toResponse(com.project.financeapi.enumSystem.CardBrandStatus.ACTIVE),
                new BankResponseDTO(
                        this.getBank().getId(),
                        this.getBank().getName(),
                        this.getBank().getCode(),
                        this.getBank().getStatus()
                )
        );
    }

    @Override
    public List<InstallmentDTO> process(List<InstallmentDTO> installments, LocalDate purchaseDate) {
        if (installments == null || installments.isEmpty()) {
            return installments;
        }

        List<InstallmentDTO> ordered = installments.stream()
                .sorted(Comparator.comparing(InstallmentDTO::parcelNumber))
                .toList();

        BigDecimal totalPurchaseAmount = ordered.stream()
                .map(InstallmentDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (getAvailableLimit().compareTo(totalPurchaseAmount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Limite insuficiente no cartão de crédito.");
        }

        // 🌟 CORREÇÃO FINAL: Usa a data da compra passada pelo Invoice (e não da primeira parcela)
        if (purchaseDate == null) {
            purchaseDate = LocalDate.now(); // Fallback
        }

        // Verifica se a data da compra já passou do dia de fechamento
        boolean afterClosing = purchaseDate.getDayOfMonth() > this.closingDay;

        // Se passou do fechamento, a fatura base cai para o mês seguinte
        LocalDate baseInvoiceDate = afterClosing
                ? purchaseDate.plusMonths(1)
                : purchaseDate;

        boolean dueInNextMonth = this.dueDay < this.closingDay;
        LocalDate firstDueDate = calculateDueDate(baseInvoiceDate, dueInNextMonth);

        return getInstallmentDTOS(ordered, firstDueDate);
    }

    @NotNull
    private static List<InstallmentDTO> getInstallmentDTOS(List<InstallmentDTO> ordered, LocalDate firstDueDate) {
        List<InstallmentDTO> processed = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            InstallmentDTO dto = ordered.get(i);

            // Para compras parceladas, cada parcela avança exatamente um mês a partir do primeiro vencimento
            LocalDate dueDate = firstDueDate.plusMonths(i);

            processed.add(new InstallmentDTO(
                    dto.amount(),
                    dto.parcelNumber(),
                    dueDate,
                    dto.accountId(),
                    dto.instrument(),
                    MovementDirection.OUTFLOW // Lançamento de cartão sempre tira dinheiro
            ));
        }
        return processed;
    }

    private LocalDate calculateDueDate(LocalDate baseInvoiceDate, boolean dueInNextMonth) {
        // Empurra para o mês seguinte caso seja o cenário de virada de mês
        LocalDate targetMonth = dueInNextMonth ? baseInvoiceDate.plusMonths(1) : baseInvoiceDate;

        // Proteção contra meses que não têm dia 31, 30 ou 29
        int day = Math.min(this.dueDay, targetMonth.lengthOfMonth());

        return LocalDate.of(targetMonth.getYear(), targetMonth.getMonth(), day);
    }
}
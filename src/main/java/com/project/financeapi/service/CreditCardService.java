package com.project.financeapi.service;

import com.project.financeapi.dto.dashboard.CreditCardSummaryDTO;
import com.project.financeapi.dto.dashboard.SnowballProjection;
import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.CreditCard;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final UserContextService userContextService;
    private final InstallmentRepository installmentRepository;

    // 🌟 Anotação obrigatória para permitir o carregamento das parcelas vinculadas
    @Transactional(readOnly = true)
    public List<CreditCardDetailsDTO> getAll() {
        User user = userContextService.getAuthenticatedUser();

        return creditCardRepository.findAllByCreatedBy_Id(user.getId())
                .stream()
                .map(CreditCard::toDTO)
                .toList();
    }

    // 🌟 Anotação obrigatória para permitir o carregamento das parcelas vinculadas
    @Transactional(readOnly = true)
    public CreditCardDetailsDTO getById(UUID id) {
        User user = userContextService.getAuthenticatedUser();

        CreditCard creditCard = creditCardRepository.findByCreatedByAndId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cartão não encontrado."));

        return creditCard.toDTO();
    }

    public CreditCardSummaryDTO getSummary() {
        String userId = userContextService.getAuthenticatedUser().getId();
        LocalDate today = LocalDate.now();

        // 1. Busca todos os cartões de crédito do usuário
        // OBS: Se o seu método no repositório tiver outro nome (ex: findByCreatedById), ajuste aqui:
        List<CreditCard> cards = creditCardRepository.findAllByCreatedBy_Id(userId);

        BigDecimal globalLimit = BigDecimal.ZERO;
        BigDecimal globalAvailable = BigDecimal.ZERO;

        long maxDaysToPay = -1;
        CreditCardSummaryDTO.BestCardDTO bestCard = null;

        // 2. Calcula Limites Globais e descobre o Melhor Cartão para Hoje
        for (CreditCard card : cards) {
            globalLimit = globalLimit.add(card.getCreditLimit() != null ? card.getCreditLimit() : BigDecimal.ZERO);
            globalAvailable = globalAvailable.add(card.getAvailableLimit() != null ? card.getAvailableLimit() : BigDecimal.ZERO);

            if (card.getStatus() != null && card.getStatus().name().equals("ACTIVE")) {
                int targetMonth = today.getMonthValue();
                int targetYear = today.getYear();

                // Regra 1: Se já passou do dia de fechamento, a fatura atual está fechada. A compra vai para o mês seguinte.
                if (today.getDayOfMonth() > card.getClosingDay()) {
                    targetMonth++;
                    if (targetMonth > 12) { targetMonth = 1; targetYear++; }
                }

                // Regra 2: Se o dia de vencimento é menor que o dia de fechamento (ex: fecha dia 25, vence dia 05)
                // O vencimento sempre ocorre no mês seguinte ao fechamento.
                if (card.getDueDay() < card.getClosingDay()) {
                    targetMonth++;
                    if (targetMonth > 12) { targetMonth = 1; targetYear++; }
                }

                LocalDate nextDue = LocalDate.of(targetYear, targetMonth, card.getDueDay());
                long daysToPay = ChronoUnit.DAYS.between(today, nextDue);

                if (daysToPay > maxDaysToPay) {
                    maxDaysToPay = daysToPay;
                    // Usa o apelido (name) ou o nome impresso no cartão
                    String cardName = card.getName() != null ? card.getName() : card.getCardHolderName();
                    bestCard = new CreditCardSummaryDTO.BestCardDTO(cardName, daysToPay, nextDue);
                }
            }
        }

        BigDecimal globalUsed = globalLimit.subtract(globalAvailable);

        // 3. Busca e formata o Gráfico Bola de Neve (Usando a Query Nativa que você criou)
        String currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<SnowballProjection> projections = installmentRepository.getCreditCardSnowballChart(userId, currentMonth);

        List<CreditCardSummaryDTO.SnowballChartDTO> snowballData = projections.stream().map(p -> {
            // Converte "2026-08" para "08/26" para ficar elegante no gráfico
            String[] parts = p.getMonth().split("-");
            String formattedMonth = parts[1] + "/" + parts[0].substring(2);
            return new CreditCardSummaryDTO.SnowballChartDTO(formattedMonth, p.getTotal());
        }).toList();

        return new CreditCardSummaryDTO(globalLimit, globalAvailable, globalUsed, bestCard, snowballData);
    }
}
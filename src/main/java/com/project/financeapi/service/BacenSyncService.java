package com.project.financeapi.service;

import com.project.financeapi.dto.integration.BacenSgsDTO;
import com.project.financeapi.entity.MarketIndex;
import com.project.financeapi.enumSystem.IndexType;
import com.project.financeapi.repository.MarketIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BacenSyncService {

    private final MarketIndexRepository marketIndexRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BACEN_CDI_URL =
            "https://api.bcb.gov.br/dados/serie/bcdata.sgs.12/dados?formato=json&dataInicial={dataInicial}&dataFinal={dataFinal}";

    private static final DateTimeFormatter BACEN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * O EventListener faz o método rodar IMEDIATAMENTE ao ligar a API para você testar.
     * O Scheduled garante que ele continue rodando sozinho à 01:00 da manhã nos próximos dias.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void syncCdiRates() {
        log.info("Iniciando sincronização de taxas CDI com o Banco Central...");

        LocalDate lastSavedDate = marketIndexRepository.findMaxDateByType(IndexType.CDI)
                .orElse(LocalDate.now().minusDays(5));

        LocalDate startDate = lastSavedDate.plusDays(1);
        LocalDate endDate = LocalDate.now();

        if (startDate.isAfter(endDate)) {
            log.info("As taxas CDI já estão atualizadas.");
            return;
        }

        String dataInicialStr = startDate.format(BACEN_DATE_FORMAT);
        String dataFinalStr = endDate.format(BACEN_DATE_FORMAT);

        try {
            BacenSgsDTO[] response = restTemplate.getForObject(
                    BACEN_CDI_URL,
                    BacenSgsDTO[].class,
                    dataInicialStr,
                    dataFinalStr
            );

            if (response != null && response.length > 0) {
                List<MarketIndex> newIndices = Arrays.stream(response)
                        .map(dto -> {
                            MarketIndex index = new MarketIndex();
                            index.setIndexType(IndexType.CDI);
                            index.setReferenceDate(LocalDate.parse(dto.data(), BACEN_DATE_FORMAT));
                            index.setRate(new BigDecimal(dto.valor()));
                            return index;
                        })
                        .toList();

                marketIndexRepository.saveAll(newIndices);
                log.info("{} novas taxas CDI importadas com sucesso.", newIndices.size());
            } else {
                log.info("Nenhuma taxa nova retornada pelo Bacen no período.");
            }

        } catch (Exception e) {
            log.error("Erro ao sincronizar taxas CDI do Banco Central: {}", e.getMessage());
        }
    }
}
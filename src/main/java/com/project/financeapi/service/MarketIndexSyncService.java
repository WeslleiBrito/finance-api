package com.project.financeapi.service;

import com.project.financeapi.dto.integration.BacenSgsResponseDTO;
import com.project.financeapi.entity.MarketIndexRate;
import com.project.financeapi.enumSystem.IndexerType;
import com.project.financeapi.integration.BacenSgsClient;
import com.project.financeapi.repository.MarketIndexRateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketIndexSyncService {

    private static final Logger log = LoggerFactory.getLogger(MarketIndexSyncService.class);

    // Códigos oficiais do SGS Banco Central
    private static final int SERIES_CDI = 12;
    private static final int SERIES_IPCA = 433;

    // Se o banco for novo, busca dos últimos 5 anos por padrão (para suportar legado)
    private static final int DEFAULT_YEARS_BACK = 5;

    private final BacenSgsClient bacenClient;
    private final MarketIndexRateRepository repository;

    // ========================================================================
    // GATILHOS DE EXECUÇÃO
    // ========================================================================

    @PostConstruct
    public void syncOnStartup() {
        log.info("Iniciando verificação de defasagem de índices de mercado...");
        syncIndex(IndexerType.CDI, SERIES_CDI);
        syncIndex(IndexerType.IPCA, SERIES_IPCA);
    }

    // Roda todos os dias (Terça a Sábado) às 08:00 AM.
    // Segunda-feira não roda porque o CDI de sexta já é capturado no sábado de manhã.
    @Scheduled(cron = "0 0 8 * * TUE-SAT")
    public void dailySync() {
        log.info("Executando Cron Job diário para atualização de índices...");
        syncIndex(IndexerType.CDI, SERIES_CDI);
    }

    // IPCA é mensal. Podemos checar todo dia 15 do mês às 09:00 AM.
    @Scheduled(cron = "0 0 9 15 * ?")
    public void monthlyIpcaSync() {
        log.info("Executando Cron Job mensal para atualização do IPCA...");
        syncIndex(IndexerType.IPCA, SERIES_IPCA);
    }

    // ========================================================================
    // LÓGICA DE NEGÓCIO
    // ========================================================================

    @Transactional
    protected void syncIndex(IndexerType indexerType, int seriesCode) {
        // 1. Descobre a última data salva no banco
        LocalDate lastSavedDate = repository.findMaxReferenceDateByIndexerType(indexerType)
                .orElse(LocalDate.now().minusYears(DEFAULT_YEARS_BACK));

        LocalDate endDate = LocalDate.now().minusDays(1); // O máximo que queremos é ontem

        // 2. Se a última data salva for antes de ontem, significa que há defasagem
        if (lastSavedDate.isBefore(endDate)) {
            LocalDate startDate = lastSavedDate.plusDays(1); // Começa a buscar do dia seguinte ao último salvo

            // 3. Bate na API do Bacen
            List<BacenSgsResponseDTO> missingRates = bacenClient.fetchRates(seriesCode, startDate, endDate);

            if (!missingRates.isEmpty()) {

                // O SEGREDO ESTÁ AQUI: Extrai a menor e a maior data real que o Bacen enviou na resposta
                LocalDate minDateBacen = missingRates.stream().map(BacenSgsResponseDTO::data).min(LocalDate::compareTo).orElse(startDate);
                LocalDate maxDateBacen = missingRates.stream().map(BacenSgsResponseDTO::data).max(LocalDate::compareTo).orElse(endDate);

                // BUSCA DE SEGURANÇA: Mapeia as datas que já existem no banco usando o range que o Bacen devolveu
                List<LocalDate> existingDates = repository
                        .findByIndexerTypeAndReferenceDateBetweenOrderByReferenceDateAsc(indexerType, minDateBacen, maxDateBacen)
                        .stream()
                        .map(MarketIndexRate::getReferenceDate)
                        .toList();

                // 4. Converte os DTOs em Entidades, filtrando o que já existe para evitar Unique Constraint Violation
                List<MarketIndexRate> entitiesToSave = missingRates.stream()
                        .filter(dto -> !existingDates.contains(dto.data())) // Ignora se a data já estiver cadastrada
                        .map(dto -> MarketIndexRate.builder()
                                .indexerType(indexerType)
                                .referenceDate(dto.data())
                                .rateValue(dto.valor())
                                .build())
                        .toList();

                // 5. Salva apenas os novos em lote (Batch Insert)
                if (!entitiesToSave.isEmpty()) {
                    repository.saveAll(entitiesToSave);
                    log.info("Sincronizados {} novos registros para o índice {}", entitiesToSave.size(), indexerType);
                } else {
                    log.info("Nenhuma taxa nova para salvar (todas já existiam) para o índice {}.", indexerType);
                }
            } else {
                log.info("Nenhuma taxa nova divulgada pelo Bacen para o índice {} no período selecionado.", indexerType);
            }
        } else {
            log.info("Índice {} já está 100% atualizado.", indexerType);
        }
    }
}
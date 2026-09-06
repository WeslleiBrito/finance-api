package com.project.financeapi.integration;

import com.project.financeapi.dto.integration.BacenSgsResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class BacenSgsClient {

    private static final Logger log = LoggerFactory.getLogger(BacenSgsClient.class);
    private static final String BACEN_SGS_URL = "https://api.bcb.gov.br/dados/serie/bcdata.sgs.{codigo}/dados?formato=json&dataInicial={dataInicial}&dataFinal={dataFinal}";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient restClient;

    public BacenSgsClient() {
        this.restClient = RestClient.create();
    }

    public List<BacenSgsResponseDTO> fetchRates(int seriesCode, LocalDate startDate, LocalDate endDate) {
        String dataInicialStr = startDate.format(FORMATTER);
        String dataFinalStr = endDate.format(FORMATTER);

        try {
            log.info("Buscando série {} do Bacen de {} até {}", seriesCode, dataInicialStr, dataFinalStr);

            List<BacenSgsResponseDTO> response = restClient.get()
                    .uri(BACEN_SGS_URL, seriesCode, dataInicialStr, dataFinalStr)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            return response != null ? response : Collections.emptyList();

        } catch (Exception e) {
            log.error("Falha ao buscar dados do Bacen para a série {}: {}", seriesCode, e.getMessage());
            return Collections.emptyList();
        }
    }
}
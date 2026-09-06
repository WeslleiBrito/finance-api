package com.project.financeapi.service;

import com.project.financeapi.dto.integration.OfficialHolidayDTO;
import com.project.financeapi.entity.Holiday;
import com.project.financeapi.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficialCalendarSyncService {

    private final HolidayRepository holidayRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // Endpoint público da Brasil API que consome os dados do governo brasileiro
    private static final String HOLIDAY_API_URL = "https://brasilapi.com.br/api/feriados/v1/{year}";

    /**
     * Roda automaticamente assim que a API liga, e também todo dia 01 de Janeiro.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 5 1 1 *") // 5h da manhã do dia 1º de Janeiro
    @Transactional
    public void syncHolidaysFromOfficialSource() {
        int currentYear = LocalDate.now().getYear();
        log.info("Buscando calendário oficial de feriados para o ano de {}...", currentYear);

        try {
            // 1. Faz a requisição HTTP GET para a fonte externa
            OfficialHolidayDTO[] response = restTemplate.getForObject(
                    HOLIDAY_API_URL,
                    OfficialHolidayDTO[].class,
                    currentYear
            );

            if (response != null && response.length > 0) {
                // 2. Converte a resposta externa para a sua Entidade local
                List<Holiday> holidays = Arrays.stream(response)
                        .map(dto -> new Holiday(dto.date(), dto.name()))
                        .toList();

                // 3. Salva/Atualiza no banco de dados local
                holidayRepository.saveAll(holidays);
                log.info("{} feriados nacionais sincronizados com sucesso.", holidays.size());
            }

        } catch (Exception e) {
            log.error("Falha ao sincronizar calendário com a fonte oficial: {}", e.getMessage());
            // Como salvamos no banco, se a API externa cair hoje, o sistema continua funcionando
            // usando os dados que já havia baixado antes.
        }
    }
}
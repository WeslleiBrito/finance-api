package com.project.financeapi.service;

import com.project.financeapi.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BusinessDayService {

    private final HolidayRepository holidayRepository;

    /**
     * Verifica se a data informada é um dia útil bancário.
     */
    @Transactional(readOnly = true)
    public boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // 1. Regra universal: Final de semana nunca é dia útil
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        // 2. Consulta a base de dados oficial do sistema (Feriados Nacionais / ANBIMA)
        boolean isHoliday = holidayRepository.existsById(date);

        // Se for feriado (true), não é dia útil (retorna false)
        return !isHoliday;
    }
}
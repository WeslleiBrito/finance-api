package com.project.financeapi.repository;

import com.project.financeapi.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {
    // Como a data é o ID, o Spring Data já tem o existsById nativo.
    // Mas podemos adicionar buscas por ano se precisarmos no futuro.
    boolean existsByDate(LocalDate date);
}
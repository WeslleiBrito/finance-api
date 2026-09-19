package com.project.financeapi.service.yield;

import com.project.financeapi.enumSystem.YieldConvention;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class YieldConversionStrategyFactory {

    private final List<YieldConversionStrategy> strategies;
    private Map<YieldConvention, YieldConversionStrategy> cache;

    @PostConstruct
    void init() {
        cache = strategies.stream()
                .collect(Collectors.toMap(YieldConversionStrategy::supports, Function.identity()));
    }

    public YieldConversionStrategy resolve(YieldConvention convention) {
        YieldConversionStrategy strategy = cache.get(convention);
        if (strategy == null) {
            throw new IllegalStateException("Nenhuma YieldConversionStrategy registrada para: " + convention);
        }
        return strategy;
    }
}

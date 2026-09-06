package com.project.financeapi.enumSystem;

public enum IndexerType {
    CDI,         // Pós-fixado (ex: 110% do CDI)
    SELIC,       // Pós-fixado (ex: 100% da Selic)
    IPCA,        // Híbrido (ex: IPCA + 6%)
    PRE_FIXED;   // Pré-fixado (ex: 12% a.a.)
}
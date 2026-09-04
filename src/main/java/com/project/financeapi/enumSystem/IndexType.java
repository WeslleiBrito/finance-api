package com.project.financeapi.enumSystem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IndexType {
    CDI(12),
    SELIC(11),
    IPCA(433);

    private final int sgsCode;
}
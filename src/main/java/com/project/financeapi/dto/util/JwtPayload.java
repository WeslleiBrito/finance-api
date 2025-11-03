package com.project.financeapi.dto.util;


import java.util.UUID;

public record JwtPayload(UUID id, String email, Integer tokenVersion) {}

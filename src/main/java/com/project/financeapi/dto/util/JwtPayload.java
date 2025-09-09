package com.project.financeapi.dto.util;


public record JwtPayload(String id, String email, Integer tokenVersion) {}

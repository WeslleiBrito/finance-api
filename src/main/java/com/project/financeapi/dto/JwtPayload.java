package com.project.financeapi.dto;


public record JwtPayload(String id, String email, Integer tokenVersion) {}

package com.project.financeapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.financeapi.dto.transaction.CreateTransactionDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

// Nova classe, sem @ExtendWith(MockitoExtension.class), sem mocks do service
class CreateTransactionDTOJsonTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void deveDesserializarPaymentDateComoJavaTimeLocalDate() throws Exception {
        String json = """
            {
              "amount": 100,
              "paymentDate": "2026-01-01",
              "installmentId": "550e8400-e29b-41d4-a716-446655440000",
              "accountId": "6fa459ea-ee8a-3ca4-894e-db77e160355e"
            }
            """;

        CreateTransactionDTO dto = mapper.readValue(json, CreateTransactionDTO.class);

        assertInstanceOf(java.time.LocalDate.class, dto.paymentDate());
    }
}

package com.project.financeapi.dto.email;
import jakarta.validation.constraints.Email;


public record EmailDTO(
        @Email(message = "Um ou mais e-mails na lista são inválidos.")
        String email

) {
}

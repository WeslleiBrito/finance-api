package com.project.financeapi.controller;

import com.project.financeapi.dto.ResponseDefault;
import com.project.financeapi.dto.email.EmailCreateRequestDTO;
import com.project.financeapi.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDefault> create(@RequestHeader("X-Auth-Token") String token,
                                                  @Valid @RequestBody EmailCreateRequestDTO dto) {
        emailService.create(token, dto.idPerson(), dto.emailList().emails());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseDefault(
                        (dto.emailList().emails().size() > 1) ?
                                "Os e-mails foram criados com sucesso" : "O e-mail foi criado com sucesso")
        );
    }

}

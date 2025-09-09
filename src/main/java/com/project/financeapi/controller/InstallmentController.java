package com.project.financeapi.controller;

import com.project.financeapi.dto.Installment.CreateInstallmentDTO;
import com.project.financeapi.dto.ResponseDefault;
import com.project.financeapi.service.InstallmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/installment")
public class InstallmentController {

    private final InstallmentService installmentService;

    public InstallmentController(InstallmentService installmentService) {
        this.installmentService = installmentService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDefault> create(@RequestHeader("X-Auth-Token") String token,
                                                  @Valid @RequestBody CreateInstallmentDTO dto) {

        installmentService.create(token, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseDefault(
                        (dto.installments().size() > 1) ?
                                "As Parcelas " + dto.installments().size() + " foram criadas com sucesso." :
                                "A parcela foi criada com sucesso"
                )
        );
    }
}

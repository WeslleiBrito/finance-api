package com.project.financeapi.service;

import com.project.financeapi.dto.Installment.CreateInstallmentDTO;
import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.dto.document.CreateDocumentRequestDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Document;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DocumentService {
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final DocumentRepository documentRepository;
    private final InstallmentService installmentService;
    private final AccountRepository accountRepository;

    public DocumentService(UserRepository userRepository, PersonRepository personRepository,
                           DocumentRepository documentRepository, InstallmentService installmentService,
                           AccountRepository accountRepository
    ) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.documentRepository = documentRepository;
        this.installmentService = installmentService;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Document create(String token, CreateDocumentRequestDTO dto){

        JwtPayload payload = JwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));

        PersonBase person = personRepository.findById(dto.personId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "A pessoa informada não existe."));

        AccountBase account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "A conta informada não existe."));


        BigDecimal subtotalInstallments = dto.installments().stream().map(InstallmentDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        if(dto.totalAmount().compareTo(subtotalInstallments) < 0){
            throw new BusinessException(
                    HttpStatus.NOT_FOUND, "O valor do total do documento é MENOR que a soma das parcelas"
            );
        }

        if(dto.totalAmount().compareTo(subtotalInstallments) > 0){
            throw new BusinessException(
                    HttpStatus.NOT_FOUND, "O valor do total do documento é MAIOR que a soma das parcelas"
            );
        }

        Document document = documentRepository.save(
                new Document(
                        dto.movementType(),
                        dto.description(),
                        dto.totalAmount(),
                        user,
                        person,
                        account

                )
        );


        List<Installment> installments = installmentService.
                create(token, new CreateInstallmentDTO(document.getId(), dto.movementType(), dto.installments()));

        document.setInstallments(installments);

        return document;


    }
}

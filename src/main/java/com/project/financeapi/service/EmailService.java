package com.project.financeapi.service;

import com.project.financeapi.dto.email.EmailDTO;
import com.project.financeapi.dto.email.EmailListDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Email;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.EmailRepository;
import com.project.financeapi.repository.PersonRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {
    private final EmailRepository emailRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    public EmailService(PersonRepository personRepository, EmailRepository emailRepository, UserRepository userRepository) {
        this.personRepository = personRepository;
        this.emailRepository = emailRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void create(String token, String personId, List<EmailDTO> listEmail){

        JwtPayload payload = JwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        PersonBase person = personRepository.findById(personId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "A pessoa informada não existe."));


        for (EmailDTO email : listEmail) {

            Email newEmail = new Email(email.email(), user, person);

            emailRepository.save(newEmail);
        }
    }

}

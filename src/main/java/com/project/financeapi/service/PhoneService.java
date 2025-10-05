package com.project.financeapi.service;

import com.project.financeapi.dto.phone.PhoneDTO;
import com.project.financeapi.dto.phone.PhoneListDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Phone;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.PersonRepository;
import com.project.financeapi.repository.PhoneRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class PhoneService {

    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final JwtUtil jwtUtil;

    public PhoneService(
            PersonRepository personRepository,
            UserRepository userRepository,
            PhoneRepository phoneRepository,
            JwtUtil jwtUtil
    ) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.phoneRepository = phoneRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public List<Phone> create(String token, String personId, List<PhoneDTO> phoneList){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        PersonBase person = personRepository.findById(personId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "A pessoa informada não existe."));

        List<Phone> phones = new ArrayList<Phone>();

        for (PhoneDTO phone : phoneList) {

            Phone newPhone = new Phone(user, person, phone.number(), phone.type());

            phones.add(phoneRepository.save(newPhone));
        }

        return phones;
    }
}

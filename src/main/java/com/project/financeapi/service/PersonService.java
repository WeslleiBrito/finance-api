package com.project.financeapi.service;

import com.project.financeapi.dto.address.AddressDTO;
import com.project.financeapi.dto.email.EmailDTO;
import com.project.financeapi.dto.person.*;
import com.project.financeapi.dto.phone.PhoneDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enums.PersonType;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.LegalEntityRepository;
import com.project.financeapi.repository.PersonRepository;
import com.project.financeapi.repository.PhysicalPersonRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import com.project.financeapi.validation.ValidateCNPJ;
import com.project.financeapi.validation.ValidateCPF;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PhysicalPersonRepository physicalPersonRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @Transactional
    public PersonResponseDTO createPhysicalPerson(String token, @org.jetbrains.annotations.NotNull PersonCreatePhysicalRequestDTO dto){
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if(!ValidateCPF.isValidCPF(dto.CPF())){
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CPF inválido.");
        }

        if(physicalPersonRepository.existsByCreatedBy_IdAndCpf(user.getId(), dto.CPF())){
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CPF");
        }

        PhysicalPerson person = new PhysicalPerson();

        person.setName(dto.name());
        person.setCreatedBy(user);
        person.setNickname(dto.nickname() != null ? dto.nickname() : dto.name());
        person.setCpf(dto.CPF());
        person.setPersonType(PersonType.INDIVIDUAL);

        return personRepository
                .save(setPerson(user, person, dto.phoneList(), dto.emailList(), dto.addressesList()))
                .toDTO();
    }

    @Transactional
    public PersonResponseDTO createLegalPerson(String token, @org.jetbrains.annotations.NotNull PersonCreateLegalRequestDTO dto){
        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if(!ValidateCNPJ.isValidCNPJ(dto.CNPJ())){
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CNPJ inválido.");
        }

        if(legalEntityRepository.existsByCreatedBy_IdAndCnpj(user.getId(), dto.CNPJ())){
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CNPJ");
        }

        LegalEntity legalEntity = new LegalEntity();

        legalEntity.setName(dto.name());
        legalEntity.setCreatedBy(user);
        legalEntity.setTradeName(dto.tradeName() != null ? dto.tradeName() : dto.name());
        legalEntity.setCnpj(dto.CNPJ());
        legalEntity.setPersonType(PersonType.LEGAL_ENTITY);

        return personRepository
                .save(setPerson(user, legalEntity, dto.phoneList(), dto.emailList(), dto.addressesList()))
                .toDTO();
    }


    public List<PersonResponseDTO> findAll(String token){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        return personRepository.findByCreatedBy(user)
                .stream()
                .map(PersonBase::toDTO).toList();


    }

    private PersonBase setPerson(
            @NotNull User user, @NotNull PersonBase person, List<PhoneDTO> phones, List<EmailDTO> emails, List<AddressDTO> addresses
    ){
        if(phones != null){
            phones.forEach(
                    item -> {
                        Phone phone = new Phone();
                        phone.setNumber(item.number());
                        phone.setType(item.type());
                        phone.setCreatedBy(user);
                        phone.setPerson(person);

                        person.getPhones().add(phone);
                    }
            );
        }

        if(emails != null){
            emails.forEach(
                    item -> {
                        Email email = new Email();
                        email.setAddress(item.email());
                        email.setCreatedBy(user);
                        email.setPerson(person);

                        person.getEmails().add(email);
                    }
            );
        }

        if(addresses != null){
            addresses.forEach(
                    item -> {
                        Address address = new Address();
                        address.setStreet(item.street());
                        address.setNumber(item.number());
                        address.setNeighborhood(item.neighborhood());
                        address.setComplement(item.complement());
                        address.setCity(item.city());
                        address.setState(item.state());
                        address.setZipCode(item.zipCode());
                        address.setPerson(person);
                        address.setCreatedBy(user);

                        person.getAddresses().add(address);
                    }
            );
        }

        return person;
    }
}

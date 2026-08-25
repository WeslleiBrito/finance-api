package com.project.financeapi.service;

import com.project.financeapi.dto.address.AddressDTO;
import com.project.financeapi.dto.email.EmailDTO;
import com.project.financeapi.dto.person.*;
import com.project.financeapi.dto.phone.PhoneDTO;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enumSystem.PersonType;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.LegalEntityRepository;
import com.project.financeapi.repository.PersonRepository;
import com.project.financeapi.repository.PhysicalPersonRepository;
import com.project.financeapi.validation.ValidateCNPJ;
import com.project.financeapi.validation.ValidateCPF;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PhysicalPersonRepository physicalPersonRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final UserContextService userContextService;


    @Transactional
    public PersonResponseDTO createPhysicalPerson(@org.jetbrains.annotations.NotNull PersonCreatePhysicalRequestDTO dto){

        User user = userContextService.getAuthenticatedUser();

        if (dto.CPF() != null && !dto.CPF().isBlank()) {
            if(!ValidateCPF.isValidCPF(dto.CPF())){
                throw new BusinessException(HttpStatus.BAD_REQUEST, "CPF inválido.");
            }

            if(physicalPersonRepository.existsByCreatedBy_IdAndCpf(user.getId(), dto.CPF())){
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CPF");
            }
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
    public PersonResponseDTO createLegalPerson(@org.jetbrains.annotations.NotNull PersonCreateLegalRequestDTO dto){

        User user = userContextService.getAuthenticatedUser();

        if (dto.CNPJ() != null && !dto.CNPJ().isBlank()) {
            if (!ValidateCNPJ.isValidCNPJ(dto.CNPJ())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "CNPJ inválido.");
            }

            if(legalEntityRepository.existsByCreatedBy_IdAndCnpj(user.getId(), dto.CNPJ())){
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CNPJ");
            }
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

    @Transactional
    public PersonResponseDTO updatePhysicalPerson(UUID id, PersonCreatePhysicalRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        // 1. Busca a pessoa física
        PhysicalPerson person = physicalPersonRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Pessoa física não encontrada."));

        // Segurança: garante que é o dono
        if (!person.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este cadastro.");
        }

        boolean isNewCpf = dto.CPF() != null && !dto.CPF().isBlank() && !dto.CPF().equals(person.getCpf());

        // Validação do novo CPF (se mudou)
        if (isNewCpf) {
            if (!ValidateCPF.isValidCPF(dto.CPF())) throw new BusinessException(HttpStatus.BAD_REQUEST, "CPF inválido.");
            if (physicalPersonRepository.existsByCreatedBy_IdAndCpf(user.getId(), dto.CPF())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CPF");
            }
        }

        // 2. Transforma as listas de DTOs em Entidades (reaproveitando sua lógica atual)
        List<Phone> mappedPhones = dto.phoneList() != null ? dto.phoneList().stream().map(p -> new Phone(user, person, p.number(), p.type())).toList() : new ArrayList<>();
        List<Email> mappedEmails = dto.emailList() != null ? dto.emailList().stream().map(e -> new Email(e.email(), user, person)).toList() : new ArrayList<>();
        List<Address> mappedAddresses = dto.addressesList() != null ? dto.addressesList().stream().map(a -> new Address(user, person, a.street(), a.number(), a.neighborhood(), a.complement(), a.city(), a.state(), a.zipCode())).toList() : new ArrayList<>();

        // 3. A própria entidade faz a atualização
        person.updateCommonData(dto.name(), dto.role());
        person.updatePhysicalData(dto.CPF(), dto.nickname());
        person.updateContactsAndAddresses(mappedPhones, mappedEmails, mappedAddresses);

        return personRepository.save(person).toDTO();
    }

    @Transactional
    public PersonResponseDTO updateLegalPerson(UUID id, PersonCreateLegalRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        // 1. Busca a pessoa jurídica
        LegalEntity person = legalEntityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Pessoa jurídica não encontrada."));

        // Segurança: garante que é o dono
        if (!person.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este cadastro.");
        }

        boolean isNewCNPJ = dto.CNPJ() != null && !dto.CNPJ().isBlank() && !dto.CNPJ().equals(person.getCnpj());

        // Validação do novo CNPJ (se mudou)

        if (isNewCNPJ) {
            if (!ValidateCNPJ.isValidCNPJ(dto.CNPJ())) throw new BusinessException(HttpStatus.BAD_REQUEST, "CNPJ inválido.");
            if (physicalPersonRepository.existsByCreatedBy_IdAndCpf(user.getId(), dto.CNPJ())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CNPJ");
            }
        }

        // 2. Transforma as listas de DTOs em Entidades (reaproveitando sua lógica atual)
        List<Phone> mappedPhones = dto.phoneList() != null ? dto.phoneList().stream().map(p -> new Phone(user, person, p.number(), p.type())).toList() : new ArrayList<>();
        List<Email> mappedEmails = dto.emailList() != null ? dto.emailList().stream().map(e -> new Email(e.email(), user, person)).toList() : new ArrayList<>();
        List<Address> mappedAddresses = dto.addressesList() != null ? dto.addressesList().stream().map(a -> new Address(user, person, a.street(), a.number(), a.neighborhood(), a.complement(), a.city(), a.state(), a.zipCode())).toList() : new ArrayList<>();

        // 3. A própria entidade faz a atualização
        person.updateCommonData(dto.name(), dto.role());
        person.updateLegalData(dto.CNPJ(), dto.tradeName());
        person.updateContactsAndAddresses(mappedPhones, mappedEmails, mappedAddresses);

        return personRepository.save(person).toDTO();
    }

    public List<PersonResponseDTO> findAll(){

        User user = userContextService.getAuthenticatedUser();

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

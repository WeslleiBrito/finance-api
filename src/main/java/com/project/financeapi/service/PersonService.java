package com.project.financeapi.service;

import com.project.financeapi.dto.address.AddressDTO;
import com.project.financeapi.dto.email.EmailDTO;
import com.project.financeapi.dto.person.*;
import com.project.financeapi.dto.phone.PhoneDTO;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enumSystem.PersonType;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.InvoiceRepository;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PhysicalPersonRepository physicalPersonRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserContextService userContextService;

    @Transactional
    public PersonResponseDTO createPhysicalPerson(@NotNull PersonCreatePhysicalRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        if (dto.CPF() != null && !dto.CPF().isBlank()) {
            if (!ValidateCPF.isValidCPF(dto.CPF())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "CPF inválido.");
            }
            if (physicalPersonRepository.existsByCreatedBy_IdAndCpf(user.getId(), dto.CPF())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CPF");
            }
        }

        PhysicalPerson person = new PhysicalPerson();
        person.setName(dto.name());
        person.setCreatedBy(user);
        person.setNickname(dto.nickname() != null ? dto.nickname() : dto.name());
        person.setCpf(dto.CPF());
        person.setPersonType(PersonType.INDIVIDUAL);
        person.setRole(dto.role());

        return personRepository
                .save(setPerson(user, person, dto.phoneList(), dto.emailList(), dto.addressesList()))
                .toDTO();
    }

    @Transactional
    public PersonResponseDTO createLegalPerson(@NotNull PersonCreateLegalRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        if (dto.CNPJ() != null && !dto.CNPJ().isBlank()) {
            if (!ValidateCNPJ.isValidCNPJ(dto.CNPJ())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "CNPJ inválido.");
            }
            if (legalEntityRepository.existsByCreatedBy_IdAndCnpj(user.getId(), dto.CNPJ())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CNPJ");
            }
        }

        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setName(dto.name());
        legalEntity.setCreatedBy(user);
        legalEntity.setTradeName(dto.tradeName() != null ? dto.tradeName() : dto.name());
        legalEntity.setCnpj(dto.CNPJ());
        legalEntity.setPersonType(PersonType.LEGAL_ENTITY);
        legalEntity.setRole(dto.role());

        return personRepository
                .save(setPerson(user, legalEntity, dto.phoneList(), dto.emailList(), dto.addressesList()))
                .toDTO();
    }

    @Transactional
    public PersonResponseDTO updatePhysicalPerson(UUID id, PersonUpdatePhysicalRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        PersonBase existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Pessoa não encontrada."));

        if (!existingPerson.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este cadastro.");
        }

        // Se era Jurídica e o usuário mandou requisição de Física, faz a transposição
        if (existingPerson instanceof LegalEntity) {
            return migrateToPhysicalPerson((LegalEntity) existingPerson, dto, user);
        }

        PhysicalPerson person = (PhysicalPerson) existingPerson;
        boolean isNewCpf = dto.CPF() != null && !dto.CPF().isBlank() && !dto.CPF().equals(person.getCpf());

        if (isNewCpf) {
            if (!ValidateCPF.isValidCPF(dto.CPF())) throw new BusinessException(HttpStatus.BAD_REQUEST, "CPF inválido.");
            if (physicalPersonRepository.existsByCreatedBy_IdAndCpf(user.getId(), dto.CPF())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CPF");
            }
        }

        // Mapeia listas sem checar nulo (garantido pelo @NotNull do DTO)
        List<Phone> mappedPhones = dto.phoneList().stream().map(p -> new Phone(user, person, p.number(), p.type())).toList();
        List<Email> mappedEmails = dto.emailList().stream().map(e -> new Email(e.email(), user, person)).toList();
        List<Address> mappedAddresses = dto.addressesList().stream().map(a -> new Address(user, person, a.street(), a.number(), a.neighborhood(), a.complement(), a.city(), a.state(), a.zipCode())).toList();

        person.updateCommonData(dto.name(), dto.role());
        person.updatePhysicalData(dto.CPF(), dto.nickname());
        person.updateContactsAndAddresses(mappedPhones, mappedEmails, mappedAddresses);

        return personRepository.save(person).toDTO();
    }

    @Transactional
    public PersonResponseDTO updateLegalPerson(UUID id, PersonUpdateLegalRequestDTO dto) {
        User user = userContextService.getAuthenticatedUser();

        PersonBase existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Pessoa não encontrada."));

        if (!existingPerson.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este cadastro.");
        }

        // Se era Física e o usuário mandou requisição de Jurídica, faz a transposição
        if (existingPerson instanceof PhysicalPerson) {
            return migrateToLegalPerson((PhysicalPerson) existingPerson, dto, user);
        }

        LegalEntity person = (LegalEntity) existingPerson;
        boolean isNewCNPJ = dto.CNPJ() != null && !dto.CNPJ().isBlank() && !dto.CNPJ().equals(person.getCnpj());

        if (isNewCNPJ) {
            if (!ValidateCNPJ.isValidCNPJ(dto.CNPJ())) throw new BusinessException(HttpStatus.BAD_REQUEST, "CNPJ inválido.");
            if (legalEntityRepository.existsByCreatedBy_IdAndCnpj(user.getId(), dto.CNPJ())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CNPJ");
            }
        }

        // Mapeia listas sem checar nulo (garantido pelo @NotNull do DTO)
        List<Phone> mappedPhones = dto.phoneList().stream().map(p -> new Phone(user, person, p.number(), p.type())).toList();
        List<Email> mappedEmails = dto.emailList().stream().map(e -> new Email(e.email(), user, person)).toList();
        List<Address> mappedAddresses = dto.addressesList().stream().map(a -> new Address(user, person, a.street(), a.number(), a.neighborhood(), a.complement(), a.city(), a.state(), a.zipCode())).toList();

        person.updateCommonData(dto.name(), dto.role());
        person.updateLegalData(dto.CNPJ(), dto.tradeName());
        person.updateContactsAndAddresses(mappedPhones, mappedEmails, mappedAddresses);

        return personRepository.save(person).toDTO();
    }

    // ==============================================================================
    // LÓGICA DE MIGRAÇÃO (TRANSPOSIÇÃO DE TIPOS)
    // ==============================================================================

    private PersonResponseDTO migrateToPhysicalPerson(LegalEntity oldEntity, PersonUpdatePhysicalRequestDTO dto, User user) {
        if (dto.CPF() != null && !dto.CPF().isBlank()) {
            if (!ValidateCPF.isValidCPF(dto.CPF())) throw new BusinessException(HttpStatus.BAD_REQUEST, "CPF inválido.");
            if (physicalPersonRepository.existsByCreatedBy_IdAndCpf(user.getId(), dto.CPF())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CPF.");
            }
        }

        PhysicalPerson newPerson = new PhysicalPerson();
        newPerson.setName(dto.name());
        newPerson.setCreatedBy(user);
        newPerson.setNickname(dto.nickname() != null ? dto.nickname() : dto.name());
        newPerson.setCpf(dto.CPF());
        newPerson.setPersonType(PersonType.INDIVIDUAL);
        newPerson.setRole(dto.role());

        newPerson = (PhysicalPerson) setPerson(user, newPerson, dto.phoneList(), dto.emailList(), dto.addressesList());

        PhysicalPerson savedPerson = personRepository.save(newPerson);

        // Transfere dependências (Faturas)
        List<Invoice> invoices = invoiceRepository.findByPersonId(oldEntity.getId());
        for (Invoice invoice : invoices) {
            invoice.setPerson(savedPerson);
        }
        invoiceRepository.saveAll(invoices);

        // O Hibernate deleta a entidade antiga e, por cascata, limpa todos os contatos velhos
        personRepository.delete(oldEntity);

        return savedPerson.toDTO();
    }

    private PersonResponseDTO migrateToLegalPerson(PhysicalPerson oldEntity, PersonUpdateLegalRequestDTO dto, User user) {
        if (dto.CNPJ() != null && !dto.CNPJ().isBlank()) {
            if (!ValidateCNPJ.isValidCNPJ(dto.CNPJ())) throw new BusinessException(HttpStatus.BAD_REQUEST, "CNPJ inválido.");
            if (legalEntityRepository.existsByCreatedBy_IdAndCnpj(user.getId(), dto.CNPJ())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe uma pessoa com este CNPJ.");
            }
        }

        LegalEntity newPerson = new LegalEntity();
        newPerson.setName(dto.name());
        newPerson.setCreatedBy(user);
        newPerson.setTradeName(dto.tradeName() != null ? dto.tradeName() : dto.name());
        newPerson.setCnpj(dto.CNPJ());
        newPerson.setPersonType(PersonType.LEGAL_ENTITY);
        newPerson.setRole(dto.role());

        newPerson = (LegalEntity) setPerson(user, newPerson, dto.phoneList(), dto.emailList(), dto.addressesList());

        LegalEntity savedPerson = personRepository.save(newPerson);

        // Transfere dependências (Faturas)
        List<Invoice> invoices = invoiceRepository.findByPersonId(oldEntity.getId());
        for (Invoice invoice : invoices) {
            invoice.setPerson(savedPerson);
        }
        invoiceRepository.saveAll(invoices);

        // O Hibernate deleta a entidade antiga e, por cascata, limpa todos os contatos velhos
        personRepository.delete(oldEntity);

        return savedPerson.toDTO();
    }

    public List<PersonResponseDTO> findAll() {
        User user = userContextService.getAuthenticatedUser();
        return personRepository.findByCreatedBy(user)
                .stream()
                .map(PersonBase::toDTO).toList();
    }

    private PersonBase setPerson(
            @NotNull User user, @NotNull PersonBase person, List<PhoneDTO> phones, List<EmailDTO> emails, List<AddressDTO> addresses
    ) {
        if (phones != null) {
            phones.forEach(item -> {
                Phone phone = new Phone();
                phone.setNumber(item.number());
                phone.setType(item.type());
                phone.setCreatedBy(user);
                phone.setPerson(person);
                person.getPhones().add(phone);
            });
        }

        if (emails != null) {
            emails.forEach(item -> {
                Email email = new Email();
                email.setAddress(item.email());
                email.setCreatedBy(user);
                email.setPerson(person);
                person.getEmails().add(email);
            });
        }

        if (addresses != null) {
            addresses.forEach(item -> {
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
            });
        }

        return person;
    }
}
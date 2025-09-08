package com.project.financeapi.service;

import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.person.PersonCreateRequestDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.LegalEntity;
import com.project.financeapi.entity.PhysicalPerson;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enums.PersonType;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.PersonRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.CnpjValidator;
import com.project.financeapi.util.CpfValidator;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final PhoneService phoneService;
    private final EmailService emailService;


    public PersonService(PersonRepository personRepository, UserRepository userRepository,
                         AddressService addressService, PhoneService phoneService, EmailService emailService) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.phoneService = phoneService;
        this.emailService = emailService;
    }

    @Transactional
    public PersonBase create(String token, PersonCreateRequestDTO dto){

        JwtPayload payload = JwtUtil.extractPayload(token);

        System.out.println(payload);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        PersonBase person = null;

        if(dto.personType().equals(PersonType.INDIVIDUAL)) {

            if(dto.CPF() == null){
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Você informou que pretende criar uma pessoa física, mas não passou o número do CPF.");
            }

            if(!CpfValidator.isValid(dto.CPF())){
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "O CPF informado é inválido.");
            }

            PhysicalPerson physicalPerson = new PhysicalPerson();

            physicalPerson.setCreatedBy(user);
            physicalPerson.setName(dto.name());
            physicalPerson.setCpf(dto.CPF());
            physicalPerson.setPersonType(dto.personType());

            if(dto.nickname() == null){
                physicalPerson.setNickname(dto.name());
            }else{
                physicalPerson.setNickname(dto.nickname());
            }

            person = personRepository.save(physicalPerson);

        } else {

            if(dto.CNPJ() == null){
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Você informou que pretende criar uma pessoa jurídica, mas não passou o número do CNPJ.");
            }

            if(!CnpjValidator.isValid(dto.CNPJ())){
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "O CNPJ informado é inválido.");
            }

            LegalEntity legalEntity = new LegalEntity();

            legalEntity.setCreatedBy(user);
            legalEntity.setCnpj(dto.CNPJ());
            legalEntity.setName(dto.name());
            legalEntity.setPersonType(dto.personType());
            legalEntity.setTradeName(dto.tradeName());

            if(dto.tradeName() != null){
                legalEntity.setTradeName(dto.tradeName());
            }else{
                legalEntity.setTradeName(dto.name());
            }

            System.out.println(legalEntity);
            person = personRepository.save(legalEntity);
        }

        if(dto.addressesList() != null){
            person.setAddresses(addressService.create(token, person.getId(), dto.addressesList()));
        }

        if(dto.emailList() != null){
            person.setEmails(emailService.create(token, person.getId(), dto.emailList()));
        }

        if(dto.phoneList() != null){
            person.setPhones(phoneService.create(token, person.getId(), dto.phoneList()));

        }

        PersonBase personFinal = personRepository.findById(person.getId()).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "A pessoa informada não existe.")
        );


        return personRepository.findById(person.getId()).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "A pessoa informada não existe.")
        );
    }
}

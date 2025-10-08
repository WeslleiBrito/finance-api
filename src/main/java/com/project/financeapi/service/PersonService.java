package com.project.financeapi.service;

import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.address.ResponseAddressDTO;
import com.project.financeapi.dto.document.DocumentResponseDTO;
import com.project.financeapi.dto.email.ResponseEmailDTO;
import com.project.financeapi.dto.person.PersonCreateRequestDTO;
import com.project.financeapi.dto.person.ResponseFinancialPersonDTO;
import com.project.financeapi.dto.person.ResponsePersonDTO;
import com.project.financeapi.dto.phone.ResponsePhoneDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.user.ResponseUserDTO;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final PhoneService phoneService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;


    public PersonService(
            PersonRepository personRepository,
            UserRepository userRepository,
            AddressService addressService,
            PhoneService phoneService,
            EmailService emailService,
            JwtUtil jwtUtil
    ) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.phoneService = phoneService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public PersonBase create(String token, PersonCreateRequestDTO dto){

        JwtPayload payload = jwtUtil.extractPayload(token);

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

    public List<ResponsePersonDTO> findAll(String token){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        List<ResponsePersonDTO> persons = personRepository.findByCreatedBy(user)
                .stream()
                .map(person -> new ResponsePersonDTO(
                        person.getId(),
                        person.getName(),
                        person.getPersonType(),
                        person instanceof PhysicalPerson ? ((PhysicalPerson) person).getCpf() : null,
                        person instanceof LegalEntity ? ((LegalEntity) person).getCnpj() : null,
                        person.getPhones().stream().map(phone -> new ResponsePhoneDTO(
                                phone.getId(),
                                phone.getNumber(),
                                phone.getType()
                        )).collect(Collectors.toList()),
                        person.getEmails().stream().map(email -> new ResponseEmailDTO(
                                email.getId(),
                                email.getAddress()
                        )).collect(Collectors.toList()),
                        person.getAddresses().stream().map(address -> new ResponseAddressDTO(
                                address.getId(),
                                address.getStreet(),
                                address.getNumber(),
                                address.getNeighborhood(),
                                address.getCity(),
                                address.getState(),
                                address.getZipCode(),
                                address.getComplement()
                        )).collect(Collectors.toList()),
                        new ResponseFinancialPersonDTO(
                                person.getInvoices().stream().map(document -> new DocumentResponseDTO(
                                        document.getId(),
                                        document.getIssueDate(),
                                        document.getStatus(),
                                        document.getQuantityInstallments(),
                                        document.getTotalAmount(),
                                        document.getTotalPaid(),
                                        document.getRemainingBalance(),
                                        new ResponseUserDTO(
                                                document.getCreatedBy().getId(),
                                                document.getCreatedBy().getName(),
                                                document.getCreatedBy().getUserStatus()
                                        ),
                                        new ResponseAccountDTO(
                                                document.getAccount().getId(),
                                                document.getAccount().getName(),
                                                document.getAccount().getType(),
                                                document.getAccount().getBalance(),
                                                document.getAccount().getStatus()
                                        ),
                                        document.getInstallments().stream()
                                                .map(installment -> new InstallmentResponseDTO(
                                                        installment.getId(),
                                                        installment.getAmount(),
                                                        installment.getCreatedAt(),
                                                        installment.getDueDate(),
                                                        installment.getMovementType(),
                                                        installment.getStatus(),
                                                        installment.getParcelNumber(),
                                                        installment.getInvoice().getId(),
                                                        new ResponseUserDTO(
                                                                installment.getCreatedBy().getId(),
                                                                installment.getCreatedBy().getName(),
                                                                installment.getCreatedBy().getUserStatus()
                                                        ),
                                                        installment.getTransactions().stream().map(transaction -> new TransactionResponseDTO(
                                                                transaction.getId(),
                                                                transaction.getInstallment().getId(),
                                                                transaction.getInstallment().getInvoice().getId(),
                                                                transaction.getAccount().getId(),
                                                                transaction.getMovementType(),
                                                                transaction.getAmount(),
                                                                transaction.getIssueDate(),
                                                                transaction.getDueDate(),
                                                                transaction.getPaymentDate(),
                                                                new ResponseUserDTO(
                                                                        transaction.getCreatedBy().getId(),
                                                                        transaction.getCreatedBy().getName(),
                                                                        transaction.getCreatedBy().getUserStatus()
                                                                ),
                                                                transaction.getObservations(),
                                                                transaction.getCreatedAt()
                                                        )).collect(Collectors.toList())

                                                )).collect(Collectors.toList())
                                )).collect(Collectors.toList())
                        )

                )).collect(Collectors.toList());

        return persons;
    }
}

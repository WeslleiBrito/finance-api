package com.project.financeapi.service;

import com.project.financeapi.dto.address.AddressDTO;
import com.project.financeapi.dto.address.AddressesListDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.Address;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.AddressRepository;
import com.project.financeapi.repository.PersonRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public AddressService(PersonRepository personRepository, UserRepository userRepository, AddressRepository addressRepository) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }


    @Transactional
    public void create(String token, String personId, List<AddressDTO> addressesList) {

        JwtPayload payload = JwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        PersonBase person = personRepository.findById(personId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "A pessoa informada não existe."));

        for(AddressDTO address : addressesList){

            Address newAddress = new Address(
                    user,
                    person,
                    address.street(),
                    address.number(),
                    address.complement(),
                    address.city(),
                    address.state(),
                    address.zipCode()
            );

            addressRepository.save(newAddress);
        }

    }

}

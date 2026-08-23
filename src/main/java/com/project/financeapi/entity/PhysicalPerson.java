package com.project.financeapi.entity;

import com.project.financeapi.dto.person.PhysicalPersonResponseDTO;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enumSystem.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "physical_person")
@Getter
@Setter
public class PhysicalPerson extends PersonBase {
    @Column(name = "nick_name", nullable = false)
    private String nickname;

    @Column(name = "cpf")
    private String cpf;

    public PhysicalPerson(User createdBy, String cpf, String name, String nickname, List<Phone> phones, List<Email> emails, List<Address> addresses) {
            super(createdBy, name, PersonType.INDIVIDUAL, phones, emails, addresses);
            this.nickname = (nickname != null) ? nickname : name;
            this.cpf = cpf;
    }

    public PhysicalPerson() {
    }

    public void updatePhysicalData(String cpf, String nickname) {
        if (cpf != null) this.setCpf(cpf);
        if (nickname != null) this.setNickname(nickname);
    }

    @Override
    public PhysicalPersonResponseDTO toDTO() {

        return new PhysicalPersonResponseDTO(
                this.getId(),
                this.getName(),
                this.getNickname(),
                this.getCpf(),
                this.getPersonType(),
                this.getPhones().stream().map(Phone::toResponse).toList(),
                this.getEmails().stream().map(Email::toResponse).toList(),
                this.getAddresses().stream().map(Address::toResponse).toList(),
                this.getInvoices().stream().map(Invoice::toResponse).toList()
        );
    }
}

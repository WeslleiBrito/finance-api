package com.project.financeapi.entity;

import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enums.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

    @Column(name = "cpf", nullable = false)
    private String cpf;

    public PhysicalPerson(User createdBy, String cpf, String name, String nickname, List<Phone> phones, List<Email> emails, List<Address> addresses) {
            super(createdBy, name, PersonType.INDIVIDUAL, phones, emails, addresses);
            this.nickname = (nickname != null) ? nickname : name;
            this.cpf = cpf;
    }

    public PhysicalPerson() {
    }
}

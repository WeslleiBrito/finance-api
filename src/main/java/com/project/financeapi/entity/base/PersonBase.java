package com.project.financeapi.entity.base;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.entity.Address;
import com.project.financeapi.entity.Email;
import com.project.financeapi.entity.Phone;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.PersonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "persons")
@ToString(onlyExplicitlyIncluded = true)
public abstract class PersonBase {

    @Id
    @Setter(AccessLevel.NONE)
    @ToString.Include
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 150)
    @ToString.Include
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type")
    @ToString.Include
    private PersonType personType;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @JsonManagedReference
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phone> phones = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Email> emails = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();


    public PersonBase(User createdBy, String name, PersonType personType, List<Phone> phones, List<Email> emails, List<Address> addresses) {
        this.createdBy = createdBy;
        this.name = name;
        this.personType = personType;
        this.phones = phones;
        this.emails = emails;
        this.addresses = addresses;
    }

    public PersonBase() {
    }

    public void addPhone(Phone phone) {
        phones.add(phone);
        phone.setPerson(this);
    }
}

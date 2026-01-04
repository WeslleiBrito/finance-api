package com.project.financeapi.entity.base;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.dto.person.PersonResponseDTO;
import com.project.financeapi.entity.*;
import com.project.financeapi.enums.PersonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
    private UUID id;

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

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Invoice> invoices = new ArrayList<>();

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

    public abstract PersonResponseDTO toDTO();

}

package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.financeapi.dto.address.ResponseAddressDTO;
import com.project.financeapi.entity.base.PersonBase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Entity
@Table(name = "addresses")
@Getter
@Setter
public class Address {


    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;


    @Column(nullable = false, length = 150)
    private String street;

    @Column(nullable = false, length = 10)
    private String number;

    @Column(length = 50)
    private String complement;

    @Column(nullable = false, length = 20)
    private String neighborhood;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 20)
    private String state;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonBase person;

    public Address() {
    }

    public Address(
       User createdBy,
       PersonBase person,
       String street,
       String number,
       String neighborhood,
       String complement,
       String city,
       String state,
       String zipCode
    )
    {
        this.street = street;
        this.number = number;
        this.neighborhood = neighborhood;
        this.complement = complement;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.createdBy = createdBy;
        this.person = person;
    }

    public ResponseAddressDTO toResponse() {
        return new ResponseAddressDTO(
                this.getId(),
                this.getStreet(),
                this.getNumber(),
                this.getNeighborhood(),
                this.getCity(),
                this.state,
                this.getZipCode(),
                this.getComplement()
        );
    }
}

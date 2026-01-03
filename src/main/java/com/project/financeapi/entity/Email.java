package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.financeapi.dto.email.ResponseEmailDTO;
import com.project.financeapi.entity.base.PersonBase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Entity
@Table(name = "emails")
@NoArgsConstructor
@Getter
@Setter
public class  Email {


    @Id
    @Setter(AccessLevel.PRIVATE)
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, length = 150, unique = true)
    private String address;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonBase person;


    public Email(String address, User createdBy, PersonBase person) {
        this.address = address;
        this.createdBy = createdBy;
        this.person = person;
    }

    public ResponseEmailDTO toResponse() {
        return new ResponseEmailDTO(
                this.getId(),
                this.getAddress()
        );
    }
}


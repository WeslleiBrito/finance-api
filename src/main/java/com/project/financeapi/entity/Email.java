package com.project.financeapi.entity;

import com.project.financeapi.entity.base.PersonBase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "emailList")
@NoArgsConstructor
@Getter
@Setter
public class  Email {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.PRIVATE)
    private String id;

    @Column(nullable = false, length = 150, unique = true)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private PersonBase person;


    public Email(String address, User createdBy, PersonBase person) {
        this.address = address;
        this.createdBy = createdBy;
        this.person = person;
    }
}


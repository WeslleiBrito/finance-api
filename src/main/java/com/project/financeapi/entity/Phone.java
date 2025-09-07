package com.project.financeapi.entity;

import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enums.PhoneType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table (name = "phones")
@Setter
@Getter
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private String id;

    @Column(nullable = false, length = 20)
    private String number;

    @Enumerated(EnumType.STRING)
    private PhoneType type; // CELULAR, FIXO, WHATSAPP, COMERCIAL etc.

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    @Setter(AccessLevel.NONE)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonBase person;

    public Phone(User createdBy, PersonBase person, String number, PhoneType type) {
        this.createdBy = createdBy;
        this.person = person;
        this.number = number;
        this.type = type;
    }

    public Phone() {
    }
}

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
@Table(name = "legal_entity")
@Getter
@Setter
public class LegalEntity extends PersonBase {
    @Column(name = "trade_name", nullable = false)
    private String tradeName;

    @Column(name = "cnpj", nullable = false)
    private String cnpj;

    public LegalEntity(User createdBy, String cnpj,  String name, String tradeName, List<Phone> phones, List<Email> emails, List<Address> addresses) {
        super(createdBy, name, PersonType.LEGAL_ENTITY, phones, emails, addresses);
        this.tradeName = (tradeName != null) ? tradeName : name;
        this.cnpj = cnpj;
    }

    public LegalEntity() {
    }

    @Override
    public PhysicalPersonResponseDTO toDTO() {

        return new PhysicalPersonResponseDTO(
                this.getId(),
                this.getName(),
                this.getTradeName(),
                this.getCnpj(),
                this.getPersonType(),
                this.getPhones().stream().map(Phone::toResponse).toList(),
                this.getEmails().stream().map(Email::toResponse).toList(),
                this.getAddresses().stream().map(Address::toResponse).toList(),
                this.getInvoices().stream().map(Invoice::toResponse).toList()
        );
    }

}

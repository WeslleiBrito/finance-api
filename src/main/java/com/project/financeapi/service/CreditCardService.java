package com.project.financeapi.service;

import com.project.financeapi.repository.CardRepository;
import lombok.Data;

@Data
public class CreditCardService {

    private final CardRepository cardRepository;


}

package com.affirm.loan.loanbalancer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Facility {

    Float amount;
    Float interestRate;
    Integer id;
    Integer bankId;
}

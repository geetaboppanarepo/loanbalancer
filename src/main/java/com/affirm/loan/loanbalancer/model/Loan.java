package com.affirm.loan.loanbalancer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Loan {
    Float interestRate;
    Integer amount;
    Integer id;
    Float defaultLikelihood;
    String state;
}

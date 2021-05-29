package com.affirm.loan.loanbalancer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class Covenant {

    Integer facilityId;
    Float maxDefaultLikelihood;
    Integer bankId;
    String bannedState;
}

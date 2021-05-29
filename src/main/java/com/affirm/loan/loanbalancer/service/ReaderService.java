package com.affirm.loan.loanbalancer.service;

import com.affirm.loan.loanbalancer.model.*;
import com.affirm.loan.loanbalancer.util.CSVUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class ReaderService {

    public ByteArrayInputStream processFiles(MultipartFile bankFile, MultipartFile facilityFile,
                                             MultipartFile covenantFile, MultipartFile loanFile) {
        try {
            //List<Bank> bankList = CSVUtil.convertToBank(bankFile.getInputStream());
            List<Facility> facilityList = CSVUtil.convertToFacility(facilityFile.getInputStream());
            List<Covenant> covenantList = CSVUtil.convertToCovenant(covenantFile.getInputStream());
            List<Loan> loanList = CSVUtil.convertToLoans(loanFile.getInputStream());

            List<Assignment> assignmentList = new ArrayList<>();
            List<Yield> yieldList = new ArrayList<>();
            for(Loan loan: loanList) {
                Integer loanAmount = loan.getAmount();
                List<Facility> fList = getAvailableFacilities(facilityList, loanAmount);
                AtomicBoolean assignedFlag = new AtomicBoolean(false);
                    fList.forEach(facilitySelected -> {
                        if(!assignedFlag.get()) {
                            List<Covenant> selectedCovenants = getCovenants(covenantList, facilitySelected);
                            Covenant covenantWithMaxDefaultLikelihood = getCovenantWithMaxDefaultLikelihood(selectedCovenants);
                            boolean covenantMatchFlag = isCovenantExists(selectedCovenants, covenantWithMaxDefaultLikelihood.getMaxDefaultLikelihood(), loan.getState(), loan.getDefaultLikelihood());
                            if (covenantMatchFlag == false) {
                                facilityList.forEach(f -> {
                                    if (f.getId() == facilitySelected.getId() && f.getBankId() == facilitySelected.getBankId() && f.getInterestRate() == facilitySelected.getInterestRate()) {
                                        f.setAmount(f.getAmount() - loanAmount);
                                    }
                                });
                                assignmentList.add(createAssignment(facilitySelected.getId(), loan.getId()));
                                assignedFlag.set(true);
                                Yield yield = createYield(yieldList, loan, facilitySelected);
                                yieldList.removeIf(x -> x.getFacilityId() == facilitySelected.getId());
                                yieldList.add(yield);

                            }
                        }
                    });

            }
            CSVUtil.createYieldFile(yieldList);
            CSVUtil.createAssignmentFile(assignmentList);
            return CSVUtil.transformToAssignmentsFile(assignmentList);

        } catch (IOException e) {
            throw new RuntimeException("fail to get data from input files: " + e.getMessage());
        }

    }

    public Assignment createAssignment(Integer facilityId, Integer loanId) {
        Assignment assignment = new Assignment();
        assignment.setFacilityId(facilityId);
        assignment.setLoanId(loanId);
        return assignment;
    }

    public List<Facility> getAvailableFacilities(List<Facility> facilityList, Integer loanAmount) {
        return facilityList.stream().filter( f-> f.getAmount() >= loanAmount)
                .sorted(Comparator.comparing(Facility::getInterestRate))
                .collect(Collectors.toList());
    }

    public boolean isCovenantExists(List<Covenant> covenants, Float maxDefaultLikelihood, String state, Float defaultLikelihood) {
        AtomicBoolean flag = new AtomicBoolean(false);
        covenants.forEach(c -> {
            if (c.getBannedState().equalsIgnoreCase(state)) {
                flag.set(true);
            } else if (maxDefaultLikelihood < defaultLikelihood) {
                flag.set(true);
            }
        });
        return flag.get();
    }

    public List<Covenant> getCovenants(List<Covenant> covenants, Facility facility) {
        return covenants.stream().filter(c -> c.getBankId() == facility.getBankId()
                && c.getFacilityId() == facility.getId()).collect(Collectors.toList());
    }

    public Covenant getCovenantWithMaxDefaultLikelihood(List<Covenant> covenants) {
        return covenants.stream().filter(sc -> sc.getMaxDefaultLikelihood() != -1).findFirst().get();
    }

    public Yield createYield(List<Yield> yields, Loan loan, Facility facility) {
        Yield yield = new Yield();
        Float expected_yield =
                (1 - loan.getDefaultLikelihood()) * loan.getInterestRate() * loan.getAmount()
                        - loan.getDefaultLikelihood() * loan.getAmount()
                        - facility.getInterestRate() * loan.getAmount();
        yield.setFacilityId(facility.getId());
        Integer existingYield = 0;
        if (yields.stream().filter(o -> o.getFacilityId().equals(facility.getId())).findFirst().isPresent()) {
            existingYield = yields.stream().filter(o -> o.getFacilityId().equals(facility.getId())).findFirst().get().getAmount();
        }
        yield.setAmount(Math.round(existingYield + expected_yield));
        return yield;
    }

}

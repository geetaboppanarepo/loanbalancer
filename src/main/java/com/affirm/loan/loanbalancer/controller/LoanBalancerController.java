package com.affirm.loan.loanbalancer.controller;

import com.affirm.loan.loanbalancer.response.ResponseMessage;
import com.affirm.loan.loanbalancer.service.ReaderService;
import com.affirm.loan.loanbalancer.util.CSVUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/loans/api")
public class LoanBalancerController {

    @Autowired
    ReaderService readerService;

    @PostMapping("/process")
    public ResponseEntity<Resource> getFiles(@RequestParam("bankFile") MultipartFile bankFile,
                                             @RequestParam("facilityFile") MultipartFile facilityFile,
                                             @RequestParam("covenantFile") MultipartFile covenantFile,
                                             @RequestParam("loanFile") MultipartFile loanFile
    ) {

        if (CSVUtil.hasCSVFormat(bankFile) && CSVUtil.hasCSVFormat(facilityFile)
                && CSVUtil.hasCSVFormat(covenantFile) && CSVUtil.hasCSVFormat(loanFile) ) {
            try {

                String filename = "assignments.csv";
                InputStreamResource file = new InputStreamResource(readerService.processFiles(bankFile, facilityFile, covenantFile, loanFile));


                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/csv"))
                        .body(file);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

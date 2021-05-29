package com.affirm.loan.loanbalancer.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.affirm.loan.loanbalancer.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.springframework.web.multipart.MultipartFile;

public class CSVUtil {
    public static String TYPE = "text/csv";

    public static boolean hasCSVFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    public static List<Bank> convertToBank(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

            List<Bank> bankList = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Bank bank = new Bank(
                        Integer.valueOf(csvRecord.get("Id")),
                        csvRecord.get("Name")
                );

                bankList.add(bank);
            }

            return bankList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Bank file: " + e.getMessage());
        }
    }

    public static List<Facility> convertToFacility(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

            List<Facility> FacilityList = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Facility facility = new Facility(
                        Float.valueOf(csvRecord.get("amount")),
                        Float.valueOf(csvRecord.get("interest_rate")),
                        Integer.valueOf(csvRecord.get("id")),
                        Integer.valueOf(csvRecord.get("bank_id"))
                );

                FacilityList.add(facility);
            }

            return FacilityList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Facility file: " + e.getMessage());
        }
    }

    public static String getLikelyhood(String value) {
        if(value == null || value.length() == 0) {
            return "-1";
        } else {
            return value;
        }
    }

    public static List<Covenant> convertToCovenant(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

            List<Covenant> covenantList = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Covenant covenant = new Covenant(
                        Integer.valueOf(csvRecord.get("facility_id")),
                        Float.valueOf(getLikelyhood(csvRecord.get("max_default_likelihood"))),
                        Integer.valueOf(csvRecord.get("bank_id")),
                        csvRecord.get("banned_state")
                );
                covenantList.add(covenant);
            }

            return covenantList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Covenant file: " + e.getMessage());
        }
    }

    public static List<Loan> convertToLoans(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

            List<Loan> loanList = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Loan loan = new Loan(
                        Float.valueOf(csvRecord.get("interest_rate")),
                        Integer.valueOf(csvRecord.get("amount")),
                        Integer.valueOf(csvRecord.get("id")),
                        Float.valueOf(csvRecord.get("default_likelihood")),
                        csvRecord.get("state")

                );

                loanList.add(loan);
            }

            return loanList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Covenant file: " + e.getMessage());
        }
    }


    public static ByteArrayInputStream transformToAssignmentsFile(List<Assignment> assignments) {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {
            for (Assignment assignment : assignments) {
                List<String> data = Arrays.asList(
                        String.valueOf(assignment.getLoanId()),
                        String.valueOf(assignment.getFacilityId())
                );

                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import bank data to CSV file: " + e.getMessage());
        }
    }

    public static void createYieldFile(List<Yield> yields) {
        final List<String> yield = yields.stream().map(item -> String.format("%s , %s", item.getFacilityId(), item.getAmount())).collect(Collectors.toList());
        try (FileWriter yieldWriter = new FileWriter("Yield.csv")) {
            yieldWriter.append("FacilityId");
            yieldWriter.append(",");
            yieldWriter.append("Yield");
            yieldWriter.append("\n");
            for(String rowData : yield) {
                yieldWriter.append(String.join(",", rowData));
                yieldWriter.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to create yields: " + e.getMessage());
        }
    }

    public static void createAssignmentFile(List<Assignment> assignments) {
        final List<String> yield = assignments.stream().map(item -> String.format("%s , %s", item.getLoanId(), item.getFacilityId())).collect(Collectors.toList());
        try (FileWriter yieldWriter = new FileWriter("Assignment.csv")) {
            yieldWriter.append("Loan Id");
            yieldWriter.append(",");
            yieldWriter.append("Facility Id");
            yieldWriter.append("\n");
            for(String rowData : yield) {
                yieldWriter.append(String.join(",", rowData));
                yieldWriter.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to create assignments: " + e.getMessage());
        }
    }

    public static ByteArrayInputStream transformToYieldFile(List<Yield> yields) {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {
            for (Yield yield : yields) {
                List<String> data = Arrays.asList(
                        String.valueOf(yield.getFacilityId()),
                        String.valueOf(yield.getAmount())
                );

                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import bank data to CSV file: " + e.getMessage());
        }
    }

}
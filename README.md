# Getting Started
Loan Balancer Application

Spring boot application that consumes loans from the stream and 
assigns each loan to a facility while respecting each facility's covenants

The below instructions will guide you through the setup and execution of the
food truck application on your local machine

Prerequisites
Java 1.8
Maven 3.8.1
postman

Assumptions
JAVA_HOME, MAVEN_HOME path, classpath is set on the user machine

Installation Steps:

Please follow below steps to run the application
1. Download & Extract source code
2. Open terminal and traverse to base directory of downloaded application code
3. Execute the following commands
   3.1 mvn compile
   3.2 mvn exec:java -Dexec.mainClass=com.affirm.loan.loanbalancer.LoanbalancerApplication
   3.3 Open postman
         3.3.1 select Post
         3.3.2 enter localhost:9999/loans/api/process
         3.3.3 select form-data and select all input files (banks.csv, facilities.csv, covenants.csv and loans.csv)
         3.3.4 It will geneate output files under loanbalancer folder (Assignment.csv and Yield.csv)


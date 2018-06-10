# Akka Bank

This is a study of how to use Akka actors and exposing them as REST endpoints. It consists of a bank with several accounts that exposes operations like get balance and deposit / withdram money. Some accounts can get loans from special loan accounts so communication between actors can be tried.

It was developed using Visual Studio Code. To run, use the command below, replacing the command line arguments (or configuring environment variables).
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--cass.contactPoint=172.18.0.1,--cass.keyspace=ledger,--cass.username=xxx,--cass.password=xxx
```

To build, run the following command:
```bash
mvn package dockerfile:build
```
# Akka Bank

This is a study of how to use Akka actors and exposing them as REST endpoints. It consists of a bank with several accounts that exposes operations like get balance and deposit / withdram money. Some accounts can get loans from special loan accounts so communication between actors can be tried.

It was developed using Visual Studio Code. To run, use:
```bash
mvn spring-boot:run
```

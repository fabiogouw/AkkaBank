package com.galore.bank;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import akka.actor.ActorSystem;

@Configuration
public class AppConfig {
    @Bean
    @Scope(value = "singleton")
    public ActorSystem actorSystem() {
        return ActorSystem.create("bank");
    }

    @Bean
    @Scope(value = "singleton")
    public AccountBag accountBag() {
        return new AccountBag();
    }

    @Bean
    public Ledger ledger() {
        return new Ledger();
    }    
}
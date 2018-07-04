package com.galore.bank;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import akka.actor.ActorSystem;

@Configuration
public class AppConfig {
    @Value("${akka.port}")
    private String _port;

    @Bean
    @Scope(value = "singleton")
    public ActorSystem actorSystem() {
        String port = "2551";
        Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + _port)
            .withFallback(ConfigFactory.load());
        return ActorSystem.create("bank", config);
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
package com.fabiogouw.bank;

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
    @Value("${cass.contactPoint}")
    private String _contactPoint;    
    @Value("${cass.username}")
    private String _username;
    @Value("${cass.password}")
    private String _password;

    @Bean
    @Scope(value = "singleton")
    public ActorSystem actorSystem() {
        Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + _port + "\n"
        + "cassandra-journal.contact-points=[" + _contactPoint + "]\n"
        + "cassandra-journal.authentication.username=" + _username + "\n"
        + "cassandra-journal.authentication.password=" + _password + "\n"
        + "cassandra-snapshot-store.contact-points=[" + _contactPoint + "]\n"
        + "cassandra-snapshot-store.authentication.username=" + _username + "\n"
        + "cassandra-snapshot-store.authentication.password=" + _password + "\n")
            .withFallback(ConfigFactory.load());
        return ActorSystem.create("bank", config);
    }

    @Bean
    public Ledger ledger() {
        return new Ledger();
    }    
}
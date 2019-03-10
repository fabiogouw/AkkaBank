package com.fabiogouw.bank;

import com.fabiogouw.bank.adapters.repository.CassandraRepository;
import com.fabiogouw.bank.adapters.repository.FakeRepository;
import com.fabiogouw.bank.core.contracts.AccountRepository;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import akka.actor.ActorSystem;

@Configuration
public class AppConfig {
    @Value("${repository.fake}")
    private Boolean _fakeLedger;

    @Value("${repository.cassandra.contactPoint}")
    private String _contactPoint;
    @Value("${repository.cassandra.username}")
    private String _username;
    @Value("${repository.cassandra.password}")
    private String _password;

    @Value("${akka.port}")
    private String _port;
    @Value("${akka.seed.host}")
    private String _akkaSeedHost;
    @Value("${akka.seed.port}")
    private String _akkaSeedPort;

    @Bean
    @Scope(value = "singleton")
    public ActorSystem actorSystem() {
        String configValues = "akka.remote.netty.tcp.port=" + _port + "\n"
        + "cassandra-journal.contact-points=[" + _contactPoint + "]\n"
        + "cassandra-journal.authentication.username=" + _username + "\n"
        + "cassandra-journal.authentication.password=" + _password + "\n"
        + "cassandra-snapshot-store.contact-points=[" + _contactPoint + "]\n"
        + "cassandra-snapshot-store.authentication.username=" + _username + "\n"
        + "cassandra-snapshot-store.authentication.password=" + _password + "\n"
        // "akka.tcp://bank@127.0.1.1:2551"
        + "akka.cluster.seed-nodes=[\"akka.tcp://bank@" + _akkaSeedHost + ":" + _akkaSeedPort + "\"]\n";
        System.out.println(configValues);
        Config config = ConfigFactory.parseString(configValues)
            .withFallback(ConfigFactory.load());
        return ActorSystem.create("bank", config);
    }

    @Bean
    public AccountRepository ledger() {
        if(_fakeLedger) {
            System.out.println("Using a fake ledger...");
            return new FakeRepository();
        }
        else {
            System.out.println("Using a Cassandra ledger...");
            return new CassandraRepository();
        }
    }
}
package com.fabiogouw.bank;

import java.util.Random;

import javax.annotation.PostConstruct;

import com.fabiogouw.bank.core.Ledger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;

@Component
public class AccountsInit {
    private final ActorSystem _system;
    private final Ledger _ledger;

    public AccountsInit(ActorSystem system, Ledger ledger) {
        _system = system;
        _ledger = ledger;
    }

    @PostConstruct
    public void init() {
        ClusterSharding.get(_system)
                       .start(AccountActor.SHARD,
                                AccountActor.props(0, _ledger),
                                ClusterShardingSettings.create(_system),
                                AccountActor.shardExtractor()
                       );      
    }
}
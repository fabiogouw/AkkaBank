package com.galore.bank;

import java.util.Random;

import javax.annotation.PostConstruct;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;

@Component
public class AccountsInit {
    private final static int TOTAL_ACCOUNTS = 1; 
    private final AccountBag _accountBag;
    private final ActorSystem _system;
    private final Ledger _ledger;

    public AccountsInit(AccountBag accountBag, ActorSystem system, Ledger ledger) {
        _accountBag = accountBag;
        _system = system;
        _ledger = ledger;
    }

    @PostConstruct
    public void init() {
        for (int i = 1; i <= TOTAL_ACCOUNTS; i++) {
            String id = String.valueOf(i);
            ActorRef accountRef = _system.actorOf(AccountActor.props(id, 0, _ledger), id);
            _accountBag.add(id , accountRef);
        }

        ClusterSharding.get(_system)
                       .start(BranchActor.SHARD,
                                BranchActor.props(_ledger),
                                ClusterShardingSettings.create(_system),
                                BranchActor.shardExtractor()
                       );        
    }
}
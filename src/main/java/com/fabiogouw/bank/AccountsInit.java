package com.fabiogouw.bank;

import javax.annotation.PostConstruct;

import com.fabiogouw.bank.adapters.actors.AccountActor;
import com.fabiogouw.bank.core.contracts.AccountRepository;
import com.fabiogouw.bank.core.contracts.Ledger;

import org.springframework.stereotype.Component;

import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;

@Component
public class AccountsInit {
    private final ActorSystem _system;
    private final AccountRepository _repository;

    public AccountsInit(ActorSystem system, AccountRepository repository) {
        _system = system;
        _repository = repository;
    }

    @PostConstruct
    public void init() {
        ClusterSharding.get(_system)
                       .start(AccountActor.SHARD,
                                AccountActor.props(0, _repository),
                                ClusterShardingSettings.create(_system),
                                AccountActor.shardExtractor()
                       );      
    }
}
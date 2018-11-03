package com.fabiogouw.bank.adapters.controllers;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.fabiogouw.bank.adapters.actors.AccountActor;
import com.fabiogouw.bank.adapters.actors.messages.BalanceRequest;
import com.fabiogouw.bank.adapters.actors.messages.DepositRequest;
import com.fabiogouw.bank.adapters.actors.messages.DepositResponse;
import com.fabiogouw.bank.adapters.actors.messages.WithdrawRequest;
import com.fabiogouw.bank.adapters.actors.messages.WithdrawResponse;
import com.fabiogouw.bank.adapters.dtos.AccountBalanceResponse;
import com.fabiogouw.bank.adapters.dtos.OperationRequest;
import com.fabiogouw.bank.adapters.dtos.OperationResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final int TIMEOUT_IN_SECONDS = 5;
    private final ActorSystem _system;

    public AccountController(ActorSystem system) {
        _system = system;
    }

    @RequestMapping(value="{accountId}/balance", method = RequestMethod.GET)
    public ResponseEntity<AccountBalanceResponse> getBalance(@PathVariable String accountId) throws Exception {
        ActorRef accountRegion = ClusterSharding.get(_system).shardRegion(AccountActor.SHARD);
        Timeout timeout = new Timeout(Duration.create(TIMEOUT_IN_SECONDS, "seconds"));
        try {
            Future<Object> future = Patterns.ask(accountRegion, new BalanceRequest(accountId), timeout);
            AccountBalanceResponse result = (AccountBalanceResponse) Await.result(future, timeout.duration());
            return ResponseEntity.ok(new AccountBalanceResponse(accountId, result.getBalance()));
        }
        catch(TimeoutException ex) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(null);
        }        
    }

    @RequestMapping(value="{accountId}/deposit", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<OperationResponse> deposit(@PathVariable String accountId, @RequestBody OperationRequest operation) throws Exception {
        if(operation.getCorrelationId() == null || operation.getCorrelationId().isEmpty()) {
            operation.setCorrelationId(UUID.randomUUID().toString());
        }        
        ActorRef accountRegion = ClusterSharding.get(_system).shardRegion(AccountActor.SHARD);
        Timeout timeout = new Timeout(Duration.create(TIMEOUT_IN_SECONDS, "seconds"));
        try {
            Future<Object> future = Patterns.ask(accountRegion, new DepositRequest(accountId, operation.getCorrelationId(), operation.getAmount()), timeout);
            DepositResponse result = (DepositResponse) Await.result(future, timeout.duration());
            return ResponseEntity.ok(new OperationResponse(operation.getCorrelationId(), operation.getAmount(), result.getCurrentBalance(), result.getSuccess()));
        }
        catch(TimeoutException ex) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new OperationResponse(operation.getCorrelationId(), operation.getAmount(), 0, false));
        }
    }

    @RequestMapping(value="{accountId}/withdraw", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<OperationResponse> withdraw(@PathVariable String accountId, @RequestBody OperationRequest operation) throws Exception {
        if(operation.getCorrelationId() == null || operation.getCorrelationId().isEmpty()) {
            operation.setCorrelationId(UUID.randomUUID().toString());
        }
        ActorRef accountRegion = ClusterSharding.get(_system).shardRegion(AccountActor.SHARD);
        Timeout timeout = new Timeout(Duration.create(TIMEOUT_IN_SECONDS, "seconds"));
        try {
            Future<Object> future = Patterns.ask(accountRegion, new WithdrawRequest(accountId, operation.getCorrelationId(), operation.getAmount()), timeout);
            WithdrawResponse result = (WithdrawResponse) Await.result(future, timeout.duration());
            return ResponseEntity.ok(new OperationResponse(operation.getCorrelationId(), operation.getAmount(), result.getCurrentBalance(), result.getSuccess()));
        }
        catch(TimeoutException ex) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new OperationResponse(operation.getCorrelationId(), operation.getAmount(), 0, false));
        }
    }  
}
package com.galore.bank;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final int TIMEOUT_IN_SECONDS = 5;
    private final AccountBag _accountBag;

    public AccountController(AccountBag accountBag) {
        _accountBag = accountBag;
    }

    @RequestMapping(value="{accountId}/balance", method = RequestMethod.GET)
    public BalanceResponse getBalance(@PathVariable String accountId) throws Exception {
        ActorRef account = _accountBag.get(accountId);
        Timeout timeout = new Timeout(Duration.create(TIMEOUT_IN_SECONDS, "seconds"));
        Future<Object> future = Patterns.ask(account, new AccountActor.BalanceRequest(), timeout);
        AccountActor.BalanceResponse result = (AccountActor.BalanceResponse) Await.result(future, timeout.duration());
        return new BalanceResponse(accountId, result.getBalance());
    }

    @RequestMapping(value="{accountId}/deposit", method = RequestMethod.POST)
    public @ResponseBody OperationResponse deposit(@PathVariable String accountId, @RequestBody OperationRequest operation) throws Exception {
        if(operation.getCorrelationId() == null || operation.getCorrelationId().isEmpty()) {
            operation.setCorrelationId(UUID.randomUUID().toString());
        }        
        ActorRef account = _accountBag.get(accountId);
        Timeout timeout = new Timeout(Duration.create(TIMEOUT_IN_SECONDS, "seconds"));
        try {
            Future<Object> future = Patterns.ask(account, new AccountActor.DepositRequest(operation.getCorrelationId(), operation.getAmount()), timeout);
            AccountActor.DepositResponse result = (AccountActor.DepositResponse) Await.result(future, timeout.duration());
            return new OperationResponse(operation.getCorrelationId(), operation.getAmount(), result.getCurrentBalance(), result.getSuccess());
        }
        catch(TimeoutException ex) {
            return new OperationResponse(operation.getCorrelationId(), operation.getAmount(), 0, false);
        }
    }

    @RequestMapping(value="{accountId}/withdraw", method = RequestMethod.POST)
    public @ResponseBody OperationResponse withdraw(@PathVariable String accountId, @RequestBody OperationRequest operation) throws Exception {
        if(operation.getCorrelationId() == null || operation.getCorrelationId().isEmpty()) {
            operation.setCorrelationId(UUID.randomUUID().toString());
        }
        ActorRef account = _accountBag.get(accountId);
        Timeout timeout = new Timeout(Duration.create(TIMEOUT_IN_SECONDS, "seconds"));
        try {
            Future<Object> future = Patterns.ask(account, new AccountActor.WithdrawRequest(operation.getCorrelationId(), operation.getAmount()), timeout);
            AccountActor.WithdrawResponse result = (AccountActor.WithdrawResponse) Await.result(future, timeout.duration());
            return new OperationResponse(operation.getCorrelationId(), operation.getAmount(), result.getCurrentBalance(), result.getSuccess());
        }
        catch(TimeoutException ex) {
            return new OperationResponse(operation.getCorrelationId(), operation.getAmount(), 0, false);
        }
    }    
}
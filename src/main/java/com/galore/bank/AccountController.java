package com.galore.bank;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

@RestController
@RequestMapping("/accounts/{accountId}")
public class AccountController {

    private final AccountBag _accountBag;

    public AccountController(AccountBag accountBag) {
        _accountBag = accountBag;
    }

    @RequestMapping(value="balance", method = RequestMethod.GET)
    public String getBalance(@PathVariable String accountId) throws Exception {
        ActorRef account = _accountBag.get(accountId);
        Timeout timeout = new Timeout(Duration.create(3, "seconds"));
        Future<Object> future = Patterns.ask(account, new AccountActor.BalanceRequest(), timeout);
        AccountActor.BalanceResponse result = (AccountActor.BalanceResponse) Await.result(future, timeout.duration());
        return "Balance: " + String.valueOf(result.getBalance());
    }

    @RequestMapping(value="transfer", method = RequestMethod.POST)
    public String post() {
        return "ok";
    }
}
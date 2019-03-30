package com.fabiogouw.bank.adapters.actors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.fabiogouw.bank.adapters.actors.messages.AccountMessage;
import com.fabiogouw.bank.adapters.actors.messages.BalanceRequest;
import com.fabiogouw.bank.adapters.actors.messages.BalanceResponse;
import com.fabiogouw.bank.adapters.actors.messages.DepositRequest;
import com.fabiogouw.bank.adapters.actors.messages.DepositResponse;
import com.fabiogouw.bank.adapters.actors.messages.InternalInitialization;
import com.fabiogouw.bank.adapters.actors.messages.InternalOperationStateUpdate;
import com.fabiogouw.bank.adapters.actors.messages.OperationResponse;
import com.fabiogouw.bank.adapters.actors.messages.WithdrawRequest;
import com.fabiogouw.bank.adapters.actors.messages.WithdrawResponse;
import com.fabiogouw.bank.core.contracts.AccountRepository;
import com.fabiogouw.bank.core.domain.Account;
import com.fabiogouw.bank.core.domain.Transaction.EntryType;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

public class AccountActor extends AbstractActorWithStash { // AbstractPersistentActorWithAtLeastOnceDelivery {

    public static Props props(BigDecimal initialBalance, AccountRepository repository) {
        return Props.create(AccountActor.class, initialBalance, repository);
    }

    public static final String SHARD = "AccountActor";
    private final LoggingAdapter _log;
    private Account _account;
    private final AccountRepository _repository;

    public AccountActor(BigDecimal initialBalance, AccountRepository repository) {
        _account = new Account(getSelf().path().name(), initialBalance);
        _log = Logging.getLogger(getContext().getSystem(), this);
        _repository = repository;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        initializeAccountActor();       
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) {
        super.preRestart(reason, message);
        initializeAccountActor(); 
    }

    @Override
    public void postStop() {
        _repository.saveAccount(_account);
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        return createRespondingReceive();
    }

    private void initializeAccountActor() {
        getContext().become(createInitializingReceive());
        CompletableFuture<Account> future = _repository.getAccount(_account.getId());
        future.thenAccept(account -> {
            getSelf().tell(new InternalInitialization(account), getSelf());
        });           
    }

    private Receive createInitializingReceive() {
        return receiveBuilder()
            .match(InternalInitialization.class, init -> {
                _account = init.getAccount();
                getContext().unbecome();
                unstashAll();
            })
            .matchAny(o -> {
                stash();
            })
            .build();
    }    

    private Receive createUpdatingReceive() {
        return receiveBuilder()
            .match(BalanceRequest.class, req -> {
                // it doesn't matter if we're updating, we can always send the current balance
                sendBalance(getSender());
            })        
            .match(InternalOperationStateUpdate.class, upd -> {
                _account = upd.getAccount();
                OperationResponse response = null;
                EntryType lastTransactionEntryType = _account.getLastTransaction().getEntryType();
                UUID lastTransactionCorrelationId = _account.getLastTransaction().getCorrelationId();
                if(lastTransactionEntryType == EntryType.DEPOSIT) {
                    response = new DepositResponse(lastTransactionCorrelationId.toString(), _account.getBalance(), true);
                }
                if(lastTransactionEntryType == EntryType.WITHDRAW) {
                    response = new WithdrawResponse(lastTransactionCorrelationId.toString(), _account.getBalance(), true);
                }
                upd.getRespondTo().tell(response, getSelf());
                getContext().unbecome();
                unstashAll();
            })
            .matchAny(o -> {
                stash();
            })
            .build();
    }

    private Receive createRespondingReceive() {
        return receiveBuilder()
            .match(BalanceRequest.class, req -> {
                sendBalance(getSender());
            })
            .match(DepositRequest.class, req -> {
                ActorRef respondTo = getSender();
                _account.Deposit(UUID.fromString(req.getCorrelationId()), req.getAmount());
                CompletableFuture<Account> future = _repository.saveAccount(_account);
                getContext().become(createUpdatingReceive());
                future.thenAccept(savedAccount -> {
                    getSelf().tell(new InternalOperationStateUpdate(savedAccount, respondTo), getSelf());
                });
            })
            .match(WithdrawRequest.class, req -> {
                ActorRef respondTo = getSender();
                Boolean withdrawOk = _account.Withdraw(UUID.fromString(req.getCorrelationId()), req.getAmount());
                if(withdrawOk) {
                    CompletableFuture<Account> future = _repository.saveAccount(_account);
                    getContext().become(createUpdatingReceive());
                    future.thenAccept(savedAccount -> {
                        getSelf().tell(new InternalOperationStateUpdate(savedAccount, respondTo), getSelf());
                    });
                }
                else {
                    respondTo.tell(new WithdrawResponse(req.getCorrelationId(), _account.getBalance(), false), getSelf());
                }
            })        
            .build();
    }

    private void sendBalance(ActorRef respondTo) {
        respondTo.tell(new BalanceResponse(_account.getBalance()), getSelf());
    }
/*
	@Override
	public String persistenceId() {
		return "account-persistence-id";
	}

	@Override
	public Receive createReceiveRecover() {
		return receiveBuilder().matchAny(any -> {}).build();
    }
*/ 
    public static ShardRegion.MessageExtractor shardExtractor() {
        return new PostShardMessageExtractor();
    }

    private static class PostShardMessageExtractor extends ShardRegion.HashCodeMessageExtractor {

        PostShardMessageExtractor() {
            super(100);
        }

        @Override
        public String entityId(Object o) {
            if (o instanceof AccountMessage) {
                return ((AccountMessage) o).getAccountId();
            }
            return null;
        }
    }
}
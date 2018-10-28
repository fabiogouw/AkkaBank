package com.fabiogouw.bank.adapters.actors;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    static class AccountMessage implements Serializable {

        private static final long serialVersionUID = 4766278085642796988L;
		private final String _accountId;
        
        public AccountMessage(String accountId) {
            _accountId = accountId;
        }
    
        public String getAccountId() {
            return _accountId;
        }
    }

    public static class BalanceRequest extends AccountMessage {
        private static final long serialVersionUID = -2216452416044790679L;

		public BalanceRequest(String accountId) {
            super(accountId);
        }
    }

    public static class BalanceResponse implements Serializable {
        private static final long serialVersionUID = 1302757287444314441L;
		private final double _balance;

        public BalanceResponse(double balance) {
            _balance = balance;
        }
        public double getBalance() {
            return _balance;
        }
    }

    static class OperationRequest extends AccountMessage {
        private static final long serialVersionUID = -188612147356070992L;
		private final String _correlationId;
        private final double _amount;
        
        public OperationRequest(String accountId, String correlationId, double amount) {
            super(accountId);
            _correlationId = correlationId;
            _amount = amount;
        }
        public String getCorrelationId() {
            return _correlationId;
        }
        public double getAmount() {
            return _amount;
        }
    }

    static class OperationResponse implements Serializable {
        private static final long serialVersionUID = -6747511039799099748L;
		private final String _correlationId;
        private final double _currentBalance;
        private final Boolean _success;
        public OperationResponse(String correlationId, double currentBalance, Boolean success) {
            _correlationId = correlationId;
            _currentBalance = currentBalance;
            _success = success;
        }

        public String getCorrelationId() {
            return _correlationId;
        }
        public double getCurrentBalance() {
            return _currentBalance;
        }        
        public Boolean getSuccess() {
            return _success;
        }
    }    

    public static class DepositRequest extends OperationRequest {
        private static final long serialVersionUID = 3515932482649506598L;

		public DepositRequest(String accountId, String correlationId, double amount) {
            super(accountId, correlationId, amount);
        }
    }

    public static class DepositResponse extends OperationResponse {
        private static final long serialVersionUID = -5136902613153736547L;

		public DepositResponse(String correlationId, double currentBalance, Boolean success) {
            super(correlationId, currentBalance, success);
        }
    }

    public static class WithdrawRequest extends OperationRequest {
        private static final long serialVersionUID = 3523795952970405852L;

		public WithdrawRequest(String accountId, String correlationId, double amount) {
            super(accountId, correlationId, amount);
        }
    }

    public static class WithdrawResponse extends OperationResponse {
		private static final long serialVersionUID = 7175374830232354388L;

		public WithdrawResponse(String correlationId, double currentBalance, Boolean success) {
            super(correlationId, currentBalance, success);
        }
    }

    static class InternalOperationStateUpdate {
        private final Account _account;
        private final ActorRef _respondTo;

        public InternalOperationStateUpdate(Account account, ActorRef respondTo) {
            _account = account;
            _respondTo = respondTo;
        }
       
        public Account getAccount() {
            return _account;
        }
        public ActorRef getRespondTo() {
            return _respondTo;
        }
    }

    static class InternalInitialization {
        private final Account _account;

        public InternalInitialization(Account account) {
            _account = account;
        }
       
        public Account getAccount() {
            return _account;
        }
    }    

    public static Props props(double initialBalance, AccountRepository repository) {
        return Props.create(AccountActor.class, initialBalance, repository);
    }

    public static final String SHARD = "AccountActor";
    private final LoggingAdapter _log;
    private Account _account;
    private final AccountRepository _repository;

    public AccountActor(double initialBalance, AccountRepository repository) {
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
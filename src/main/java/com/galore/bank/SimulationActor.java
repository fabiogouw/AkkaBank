package com.galore.bank;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

public class SimulationActor extends AbstractActor {

    static class StartRequest {
        private int _maxIterations;
        public StartRequest(int maxIterations) {
            _maxIterations = maxIterations;
        }
        public int getMaxIterations() {
            return _maxIterations;
        }
    }

    static Props props(String id, AccountBag accountBag) {
        return Props.create(SimulationActor.class, id, accountBag);
    }

    private final LoggingAdapter _log;
    private final String _id;
    private final AccountBag _accountBag;
    private double _sum = 0;
    private ActorRef _respondSumTo;

    public SimulationActor(String id, AccountBag accountBag) {
        _id = id;
        _accountBag = accountBag;
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
        .match(StartRequest.class, req -> {
            _log.info("Simulation for " + _id);
            for(int i = 0; i < 10; i++) {
                ActorRef transferRef = getContext().actorOf(TransferActor.props(_accountBag));
                Thread.sleep((long)Math.random() * 1000);
                transferRef.tell(new TransferActor.TransferRequest(UUID.randomUUID().toString(), _accountBag.getRandomAccountId(), _accountBag.getRandomAccountId(), Math.random() * 1000), getSelf());
            }
            if(req.getMaxIterations() > 0) {
                Thread.sleep((long)Math.random() * 5000);
                getSelf().tell(new StartRequest(req.getMaxIterations() - 1), getSelf());
            }
        })
        .build();
    }
}
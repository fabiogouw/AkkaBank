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

    static Props props(String id) {
        return Props.create(SimulationActor.class, id);
    }

    private static final int TOTAL_ACCOUNTS = 1000;
    private final LoggingAdapter _log;
    private final String _id;

    public SimulationActor(String id) {
        _id = id;
        _log = Logging.getLogger(getContext().getSystem(), this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
        .match(StartRequest.class, req -> {
            _log.info("Simulation for " + _id);
            Random rand = new Random();
            for(int i = 0; i < 100; i++) {
                ActorRef transferRef = getContext().actorOf(TransferActor.props());
                String accountFrom = String.valueOf(rand.nextInt(TOTAL_ACCOUNTS) + 1);
                String accountTo = String.valueOf(rand.nextInt(TOTAL_ACCOUNTS) + 1);
                transferRef.tell(new TransferActor.TransferRequest(UUID.randomUUID().toString(), accountFrom, accountTo, Math.random() * 1000), getSelf());
            }
            if(req.getMaxIterations() > 0) {
                getSelf().tell(new StartRequest(req.getMaxIterations() - 1), getSelf());
            }
        })
        .build();
    }
}
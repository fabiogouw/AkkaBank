package com.galore.bank;

import java.util.Date;
import java.util.UUID;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

public class BranchActor extends AbstractActor {

    private final LoggingAdapter _log;
    private final Ledger _ledger;
    private String _id;

    public static final String SHARD = "BranchActor";

    public BranchActor(Ledger ledger) {
        _ledger = ledger;
        _log = Logging.getLogger(getContext().getSystem(), this);
        _log.info("BranchActor created.");
    }

    static Props props(Ledger ledger) {
        return Props.create(BranchActor.class, ledger);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
        .match(NewAccount.class, m -> {
            getSender().tell(m, self());
        })
            .build();
    }

    public static ShardRegion.MessageExtractor shardExtractor() {
        return new PostShardMessageExtractor();
    }

    private static class PostShardMessageExtractor extends ShardRegion.HashCodeMessageExtractor {

        PostShardMessageExtractor() {
            super(100);
        }

        @Override
        public String entityId(Object o) {
            if (o instanceof NewAccount) {
                return ((NewAccount) o).getId();
            }
            return null;
        }
    }
}
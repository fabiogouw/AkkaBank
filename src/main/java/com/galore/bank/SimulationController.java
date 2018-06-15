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
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

@RestController
@RequestMapping("/simulations")
public class SimulationController {

    private final AccountBag _accountBag;
    private final ActorSystem _system;

    public SimulationController(AccountBag accountBag, ActorSystem system) {
        _accountBag = accountBag;
        _system = system;
    }

    @RequestMapping(value="", method = RequestMethod.POST)
    public @ResponseBody String create() throws Exception {
        String id = UUID.randomUUID().toString();
        ActorRef simulationRef = _system.actorOf(SimulationActor.props(id, _accountBag), id);
        simulationRef.tell(new SimulationActor.StartRequest(20), ActorRef.noSender());
        return id;
    }    
}
package com.fabiogouw.bank;

import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@RestController
@RequestMapping("/simulations")
public class SimulationController {

    private final ActorSystem _system;

    public SimulationController(ActorSystem system) {
        _system = system;
    }

    @RequestMapping(value="", method = RequestMethod.POST)
    public @ResponseBody String create() throws Exception {
        String id = UUID.randomUUID().toString();
        ActorRef simulationRef = _system.actorOf(SimulationActor.props(id), id);
        simulationRef.tell(new SimulationActor.StartRequest(200), ActorRef.noSender());
        return id;
    }    
}
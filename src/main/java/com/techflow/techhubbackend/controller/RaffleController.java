package com.techflow.techhubbackend.controller;

import com.techflow.techhubbackend.model.RaffleModel;
import com.techflow.techhubbackend.service.RaffleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_HEADER_STRING;

@RestController
@RequestMapping("/raffle")
public class RaffleController {

    @Autowired
    private RaffleService raffleService;

    @GetMapping("/active")
    public RaffleModel getActiveRaffle() throws ExecutionException, InterruptedException {
        return raffleService.getActiveRaffle();
    }

    @GetMapping("/previous")
    public RaffleModel getPreviousRaffle() throws ExecutionException, InterruptedException {
        return raffleService.getPreviousRaffle();
    }

    @GetMapping("{id}")
    public RaffleModel getRaffle(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return raffleService.getRaffleById(id);
    }

    @GetMapping("")
    public List<RaffleModel> getAllRaffles() throws ExecutionException, InterruptedException {
        return raffleService.getAllRaffles();
    }

    @PostMapping("/register")
    public void registerUser(@RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        raffleService.registerUser(jwt);
    }
}

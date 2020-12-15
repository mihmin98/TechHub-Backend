package com.techflow.techhubbackend.controller;

import com.techflow.techhubbackend.model.UserModel;
import com.techflow.techhubbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("{email}")
    public UserModel getUser(@PathVariable("email") String email) throws InterruptedException, ExecutionException {
        return userService.getUserDetails(email);
    }

    @PostMapping("")
    public void createUser(@RequestBody UserModel user) throws InterruptedException, ExecutionException {
        userService.saveUserDetails(user, true);
    }

    @PutMapping("{email}")
    public void updateUser(@PathVariable("email") String email, @RequestBody UserModel user) throws InterruptedException, ExecutionException {
        userService.updateUserDetails(email, user);
    }

    @DeleteMapping("{email}")
    public void deleteUser(@PathVariable("email") String email) throws ExecutionException, InterruptedException {
        userService.deleteUser(email);
    }
}

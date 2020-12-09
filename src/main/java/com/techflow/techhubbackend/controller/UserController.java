package com.techflow.techhubbackend.controller;

import com.techflow.techhubbackend.model.UserModel;
import com.techflow.techhubbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/getUserDetails")
    public UserModel getUser(@RequestParam String name ) throws InterruptedException, ExecutionException {
        return userService.getUserDetails(name);
    }

    @PostMapping("/createUser")
    public String createUser(@RequestBody UserModel user ) throws InterruptedException, ExecutionException {
        return userService.saveUserDetails(user);
    }

    @PutMapping("/updateUser")
    public String updateUser(@RequestBody UserModel user  ) throws InterruptedException, ExecutionException {
        return userService.updateUserDetails(user);
    }

    @DeleteMapping("/deleteUser")
    public String deleteUser(@RequestParam String name){
        return userService.deleteUser(name);
    }

    @GetMapping("/requestUserLogIn")
    public UserModel requestUserLogIn(@RequestParam String username, @RequestParam String password ) throws InterruptedException, ExecutionException {
        return userService.requestUserLogIn(username, password);
    }
}

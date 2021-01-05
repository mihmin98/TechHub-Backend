package com.techflow.techhubbackend.controller;

import com.techflow.techhubbackend.model.UserModel;
import com.techflow.techhubbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_HEADER_STRING;

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

    @GetMapping("sortByScore/{userCount}")
    public List<UserModel> SortUsersByPointsAndTrophies(@PathVariable("userCount") Integer userCount) throws InterruptedException, ExecutionException {
        return userService.SortUsersByPointsAndTrophies(userCount);
    }

    @GetMapping("")
    public UserModel getCurrentUser(@RequestHeader(AUTH_HEADER_STRING) String jwt) throws InterruptedException, ExecutionException {
        return userService.getCurrentUser(jwt);
    }
}

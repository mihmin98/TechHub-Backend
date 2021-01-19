package com.techflow.techhubbackend.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.model.ThreadModel;
import com.techflow.techhubbackend.service.PostService;
import com.techflow.techhubbackend.service.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_HEADER_STRING;
import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_TOKEN_PREFIX;

@RestController
@RequestMapping("/thread")
public class ThreadController {

    @Autowired
    ThreadService threadService;

    @Autowired
    PostService postService;

    @GetMapping("/title/{title}")
    public List<ThreadModel> getThreadsByTitle(@PathVariable("title") String title) throws ExecutionException, InterruptedException {
        return threadService.getThreadsByTitle(title, false);
    }

    @GetMapping("/vip/title/{title}")
    public List<ThreadModel> getVIPThreadsByTitle(@PathVariable("title") String title, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        if (!getUserVipStatus(jwt))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not a VIP");
        return threadService.getThreadsByTitle(title, true);
    }

    @GetMapping("")
    public List<ThreadModel> getAllThreads() throws ExecutionException, InterruptedException {
        return threadService.getAllThreads(false);
    }

    @GetMapping("/vip")
    public List<ThreadModel> getAllVIPThreads(@RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        if (!getUserVipStatus(jwt))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not a VIP");
        return threadService.getAllThreads(true);
    }

    @GetMapping("{id}")
    public ThreadModel getThread(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return threadService.getThread(id);
    }

    @PostMapping("")
    public String createThread(@RequestBody ThreadModel thread) throws ExecutionException, InterruptedException, JsonProcessingException {
        return threadService.createThread(thread);
    }

    @PutMapping("{id}")
    public void updateThread(@PathVariable("id") String id, @RequestBody ThreadModel thread) throws ExecutionException, InterruptedException {
        threadService.updateThread(id, thread);
    }

    @DeleteMapping("{id}")
    public void deleteThread(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        threadService.deleteThread(id);
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return threadService.getCategories();
    }

    @GetMapping("/categories/{category}")
    public List<ThreadModel> getThreadsByCategory(@PathVariable("category") String category) throws ExecutionException, InterruptedException {
        return threadService.getThreadsByCategory(category, false);
    }

    @GetMapping("/vip/categories/{category}")
    public List<ThreadModel> getVIPThreadsByCategory(@PathVariable("category") String category, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        if (!getUserVipStatus(jwt))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not a VIP");
        return threadService.getThreadsByCategory(category, true);
    }

    @GetMapping("{id}/posts")
    public List<PostModel> getPostsByThreadId(@PathVariable("id") String threadId) throws ExecutionException, InterruptedException {
        return postService.getPostsByThreadId(threadId);
    }

    @GetMapping("{id}/postsCount")
    public Long getPostsCountByThreadId(@PathVariable("id") String threadId) throws ExecutionException, InterruptedException {
        return postService.getPostsCountByThreadId(threadId);
    }

    private boolean getUserVipStatus(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        return decodedJWT.getClaim("userVipStatus").asBoolean();
    }
}

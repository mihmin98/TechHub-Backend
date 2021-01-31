package com.techflow.techhubbackend.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.model.UserType;
import com.techflow.techhubbackend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_HEADER_STRING;
import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_TOKEN_PREFIX;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    PostService postService;

    @GetMapping("")
    public List<PostModel> getAllPosts() throws ExecutionException, InterruptedException {
        return postService.getAllPosts();
    }

    @GetMapping("{id}")
    public PostModel getPost(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return postService.getPost(id);
    }

    @PostMapping("")
    public String createPost(@RequestBody PostModel post) throws ExecutionException, InterruptedException, JsonProcessingException {
        return postService.createPost(post);
    }

    @PutMapping("{id}")
    public void updatePost(@PathVariable("id") String id, @RequestBody PostModel post, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        postService.updatePost(id, post, getUserTypeFromJwt(jwt));
    }

    @DeleteMapping("{id}")
    public void deletePost(@PathVariable("id") String id, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        postService.deletePost(id, getUserTypeFromJwt(jwt));
    }

    @GetMapping("postsByThreadId/{threadId}")
    public List<PostModel> getPostsByThreadId(@PathVariable("threadId") String threadId) throws ExecutionException, InterruptedException {
        return postService.getPostsByThreadId(threadId);
    }

    @PutMapping("{id}/upvote")
    public void upvotePost(@PathVariable("id") String id, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        postService.upvotePost(id, jwt);
    }

    @PutMapping("{id}/downvote")
    public void downvotePost(@PathVariable("id") String id, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        postService.downvotePost(id, jwt);
    }

    @PutMapping("{id}/removeUpvote")
    public void removeUpvotePost(@PathVariable("id") String id, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        postService.removeUpvotePost(id, jwt);
    }

    @PutMapping("{id}/removeDownvote")
    public void removeDownvotePost(@PathVariable("id") String id, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        postService.removeDownvotePost(id, jwt);
    }

    @PutMapping("{id}/awardTrophy")
    public void awardTrophy(@PathVariable("id") String id, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        postService.awardTrophy(id, jwt);
    }

    private UserType getUserTypeFromJwt(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        return UserType.valueOf(decodedJWT.getClaim("userType").asString());
    }
}

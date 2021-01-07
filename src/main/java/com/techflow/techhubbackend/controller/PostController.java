package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
    public void updatePost(@PathVariable("id") String id, @RequestBody PostModel post) throws ExecutionException, InterruptedException {
        postService.updatePost(id, post);
    }

    @DeleteMapping("{id}")
    public void deletePost(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        postService.deletePost(id);
    }

    @PutMapping("{id}/upvote/{email}")
    public void upvotePost(@PathVariable("id") String id, @PathVariable("email") String email) throws ExecutionException, InterruptedException {
        postService.upvotePost(id, email);
    }

    @PutMapping("{id}/downvote/{email}")
    public void downvotePost(@PathVariable("id") String id, @PathVariable("email") String email) throws ExecutionException, InterruptedException {
        postService.downvotePost(id, email);
    }

    @PutMapping("{id}/removeUpvote/{email}")
    public void removeUpvotePost(@PathVariable("id") String id, @PathVariable("email") String email) throws ExecutionException, InterruptedException {
        postService.removeUpvotePost(id, email);
    }

    @PutMapping("{id}/removeDownvote/{email}")
    public void removeDownvotePost(@PathVariable("id") String id, @PathVariable("email") String email) throws ExecutionException, InterruptedException {
        postService.removeDownvotePost(id, email);
    }

    @PutMapping("{id}/awardTrophy")
    public void awardTrophy(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        postService.awardTrophy(id);
    }
}

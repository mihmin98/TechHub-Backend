package com.techflow.techhubbackend.controller;

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
    public String createPost(@RequestBody PostModel post) throws ExecutionException, InterruptedException {
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
}

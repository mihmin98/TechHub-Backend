package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.model.ThreadModel;
import com.techflow.techhubbackend.service.PostService;
import com.techflow.techhubbackend.service.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/thread")
public class ThreadController {

    @Autowired
    ThreadService threadService;

    @Autowired
    PostService postService;

    @GetMapping("/title/{title}")
    public List<ThreadModel> getThreadsByTitle(@PathVariable("title") String title) throws ExecutionException, InterruptedException {
        return threadService.getThreadsByTitle(title);
    }

    @GetMapping("")
    public List<ThreadModel> getAllThreads() throws ExecutionException, InterruptedException {
        return threadService.getAllThreads();
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
        return threadService.getThreadsByCategory(category);
    }

    @GetMapping("{id}/posts")
    public List<PostModel> getPostsByThreadId(@PathVariable("id") String threadId) throws ExecutionException, InterruptedException {
        return postService.getPostsByThreadId(threadId);
    }

    @GetMapping("{id}/postsCount")
    public Long getPostsCountByThreadId(@PathVariable("id") String threadId) throws ExecutionException, InterruptedException {
        return postService.getPostsCountByThreadId(threadId);
    }
}

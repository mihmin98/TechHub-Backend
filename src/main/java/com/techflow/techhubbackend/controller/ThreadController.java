package com.techflow.techhubbackend.controller;

import com.techflow.techhubbackend.model.ThreadModel;
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

    @GetMapping("")
    public List<ThreadModel> getAllThreads() throws ExecutionException, InterruptedException {
        return threadService.getAllThreads();
    }

    @GetMapping("{id}")
    public ThreadModel getThread(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return threadService.getThread(id);
    }

    @PostMapping("")
    public String createThread(@RequestBody ThreadModel thread) throws ExecutionException, InterruptedException {
        return threadService.createThread(thread);
    }

    @PutMapping("{id}")
    public void updateThread(@PathVariable("id") String id, ThreadModel thread) throws ExecutionException, InterruptedException {
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
}

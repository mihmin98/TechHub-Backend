package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.model.ReportModel;
import com.techflow.techhubbackend.model.ThreadModel;
import com.techflow.techhubbackend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("")
    public List<ReportModel> getAllReports() throws ExecutionException, InterruptedException {
        return reportService.getSortedReportsByImportance();
    }

    @GetMapping("{id}")
    public ReportModel getReport(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return reportService.getReport(id);
    }

    @PostMapping("")
    public String createReport(@RequestBody ReportModel report) throws ExecutionException, InterruptedException, JsonProcessingException {
        return reportService.createReport(report);
    }

    @PutMapping("{id}")
    public void updateReport(@PathVariable("id") String id, @RequestBody ReportModel report) throws ExecutionException, InterruptedException {
        reportService.updateReport(id, report);
    }

    @DeleteMapping("{id}")
    public void deleteReport(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        reportService.deleteReport(id);
    }

    @GetMapping("/types")
    public List<String> getReportTypes() {
        return reportService.getReportTypes();
    }

    @GetMapping("/reportsByItem/{id}")
    public List<ReportModel> getReportsByReportedItemIdId(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return reportService.getReportsByReportedItemIdId(id);
    }

    @GetMapping("/posts")
    public List<PostModel> getReportedPosts() throws ExecutionException, InterruptedException {
        return reportService.getReportedPosts();
    }

    @GetMapping("/threads")
    public List<ThreadModel> getReportedThreads() throws ExecutionException, InterruptedException {
        return reportService.getReportedThreads();
    }
}

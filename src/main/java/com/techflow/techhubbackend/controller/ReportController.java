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

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_HEADER_STRING;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("")
    public List<ReportModel> getAllReports( @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        return reportService.getSortedReportsByImportance(jwt);
    }

    @GetMapping("{id}")
    public ReportModel getReport(@PathVariable("id") String id,  @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        return reportService.getReport(id, jwt);
    }

    @PostMapping("")
    public String createReport(@RequestBody ReportModel report, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException, JsonProcessingException {
        return reportService.createReport(report, jwt);
    }

    @PutMapping("{id}")
    public void updateReport(@PathVariable("id") String id, @RequestBody ReportModel report, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        reportService.updateReport(id, report, jwt);
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
    public List<ReportModel> getReportsByReportedItemIdId(@PathVariable("id") String id, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        return reportService.getReportsByReportedItemIdId(id, jwt);
    }

    @GetMapping("/posts")
    public List<PostModel> getReportedPosts(@RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        return reportService.getReportedPosts(jwt);
    }

    @GetMapping("/threads")
    public List<ThreadModel> getReportedThreads(@RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException {
        return reportService.getReportedThreads(jwt);
    }
}

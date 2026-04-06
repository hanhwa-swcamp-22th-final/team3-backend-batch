package com.ohgiraffers.team3backendbatch.api.query.controller;

import com.ohgiraffers.team3backendbatch.api.common.dto.ApiResponse;
import com.ohgiraffers.team3backendbatch.api.query.dto.BatchExecutionLogResponse;
import com.ohgiraffers.team3backendbatch.api.query.dto.BatchJobExecutionResponse;
import com.ohgiraffers.team3backendbatch.api.query.dto.BatchJobSummaryResponse;
import com.ohgiraffers.team3backendbatch.batch.common.launcher.BatchJobQueryFacade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class BatchJobQueryController {

    private final BatchJobQueryFacade batchJobQueryFacade;

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<BatchJobSummaryResponse>>> getJobs() {
        return ResponseEntity.ok(ApiResponse.success(batchJobQueryFacade.getJobSummaries()));
    }

    @GetMapping("/jobs/{executionId}/status")
    public ResponseEntity<ApiResponse<BatchJobExecutionResponse>> getJobStatus(@PathVariable Long executionId) {
        return ResponseEntity.ok(ApiResponse.success(batchJobQueryFacade.getExecution(executionId)));
    }

    @GetMapping("/system-logs")
    public ResponseEntity<ApiResponse<List<BatchExecutionLogResponse>>> getExecutionLogs() {
        return ResponseEntity.ok(ApiResponse.success(batchJobQueryFacade.getExecutionLogs()));
    }
}
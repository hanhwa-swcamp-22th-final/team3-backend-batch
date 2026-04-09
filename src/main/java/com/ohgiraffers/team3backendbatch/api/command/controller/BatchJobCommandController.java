package com.ohgiraffers.team3backendbatch.api.command.controller;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchRequest;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchResponse;
import com.ohgiraffers.team3backendbatch.api.common.dto.ApiResponse;
import com.ohgiraffers.team3backendbatch.batch.common.launcher.BatchJobLauncherFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batch/jobs")
@RequiredArgsConstructor
public class BatchJobCommandController {

    private final BatchJobLauncherFacade batchJobLauncherFacade;

    @PostMapping("/{jobName}/executions")
    public ResponseEntity<ApiResponse<BatchJobLaunchResponse>> launch(
        @PathVariable String jobName,
        @RequestBody @Valid BatchJobLaunchRequest request
    ) {
        BatchJobLaunchResponse response = batchJobLauncherFacade.launch(jobName, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(response));
    }
}
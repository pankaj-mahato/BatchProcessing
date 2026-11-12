package com.example.spring_batch.web;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

	private final JobLauncher jobLauncher;

	private final Job importPeopleJob;

	public BatchJobController(JobLauncher jobLauncher, @Qualifier("importPeopleJob") Job importPeopleJob) {
		this.jobLauncher = jobLauncher;
		this.importPeopleJob = importPeopleJob;
	}

	@PostMapping("/people/import")
	public ResponseEntity<JobLaunchResponse> importPeople() {
		try {
			JobExecution execution = jobLauncher.run(importPeopleJob, new JobParametersBuilder().toJobParameters());
			return ResponseEntity.accepted()
					.body(new JobLaunchResponse(execution.getId(), execution.getStatus().toString()));
		}
		catch (JobExecutionException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new JobLaunchResponse(null, "FAILED: " + ex.getMessage()));
		}
	}

	public record JobLaunchResponse(Long jobExecutionId, String status) {
	}

}

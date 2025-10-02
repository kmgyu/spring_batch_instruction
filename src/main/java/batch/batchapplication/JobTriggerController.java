package batch.batchapplication;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class JobTriggerController {

  private final JobLauncher jobLauncher;
  private final Job preheatUsersJob;
  private final Job userCreateJob;

  @PostMapping("/run/import")
  public ResponseEntity<String> runPreheat(@RequestParam(required=false) String requestId) throws Exception {
    JobParameters params = new JobParametersBuilder()
            .addString("requestId", Optional.ofNullable(requestId).orElse(UUID.randomUUID().toString()))
            .addLong("time", System.currentTimeMillis()) // 동일 파라미터로 재실행 방지
            .toJobParameters();

    JobExecution exec = jobLauncher.run(preheatUsersJob, params);
    return ResponseEntity.ok("Launched: " + exec.getId());
  }

  @PostMapping("/run/create")
  public ResponseEntity<String> runCreate(@RequestParam(required=false) String requestId) throws Exception {
    JobParameters params = new JobParametersBuilder()
            .addString("requestId", Optional.ofNullable(requestId).orElse(UUID.randomUUID().toString()))
            .addLong("time", System.currentTimeMillis()) // 동일 파라미터로 재실행 방지
            .toJobParameters();

    JobExecution exec = jobLauncher.run(userCreateJob, params);
    return ResponseEntity.ok("Launched: " + exec.getId());
  }
}

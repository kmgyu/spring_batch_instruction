package batch.batchapplication.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SimpleJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean
  public Job helloJob(Step helloStep) {
    return new JobBuilder("helloJob", jobRepository)
            .start(helloStep)
            .build();
  }

  @Bean
  public Step helloStep(Tasklet sayHelloTasklet) {
    return new StepBuilder("helloStep", jobRepository)
            .tasklet(sayHelloTasklet, transactionManager)
            .build();
  }
}

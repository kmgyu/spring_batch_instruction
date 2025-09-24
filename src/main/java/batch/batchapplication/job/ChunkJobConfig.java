package batch.batchapplication.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ChunkJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager tx;

  @Bean
  public Job importUserJob(Step importStep) {
    return new JobBuilder("importUserJob", jobRepository)
            .start(importStep)
            .build();
  }

  @Bean
  public Step importStep(ItemReader<String> reader,
                         ItemProcessor<String, String> processor,
                         ItemWriter<String> writer) {
    return new StepBuilder("importStep", jobRepository)
            .<String, String>chunk(100, tx) // 트랜잭션은 청크 단위 커밋
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
  }

  @Bean
  public ItemReader<String> reader() {
    List<String> items = List.of("a","b","c");
    return new ListItemReader<>(items);
  }

  @Bean
  public ItemProcessor<String, String> processor() {
    return item -> item.toUpperCase();
  }

  @Bean
  public ItemWriter<String> writer() {
    return items -> {
      // DB 저장, 외부 API 호출 등
      System.out.println("write: " + items);
    };
  }
}

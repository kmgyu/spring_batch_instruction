package batch.batchapplication.job;

import batch.batchapplication.auth.domain.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CreateUserJobConfig {

  private static final int TOTAL_COUNT = 10_000;  // 생성할 User 수
  private static final int CHUNK_SIZE  = 100;     // 커밋 단위

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final EntityManagerFactory entityManagerFactory;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public Job userCreateJob() {
    return new JobBuilder("userCreateJob", jobRepository)
            .start(userStep())
            .build();
  }

  @Bean
  public Step userStep() {
    return new StepBuilder("userStep", jobRepository)
            .<User, User>chunk(CHUNK_SIZE, transactionManager)
            .reader(userReader())
            .writer(userWriter())
            .build();
  }

  @Bean
  public AbstractItemCountingItemStreamItemReader<User> userReader() {
    return new AbstractItemCountingItemStreamItemReader<>() {
      private Integer count = 0;

      {
        setName("userReader");
      }

      @Override
      protected User doRead() {
        if (count++ < TOTAL_COUNT) {
          return User.builder()
                  .username("user" + count.toString()) // just for an easy way to login
                  .password(passwordEncoder.encode(count.toString()))
                  .backdoor(count.toString())
                  .build();
        }
        return null; // end of stream
      }

      @Override
      protected void doOpen() { }

      @Override
      protected void doClose() { }
    };
  }

  @Bean
  public ItemWriter<User> userWriter() {
    JpaItemWriter<User> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(entityManagerFactory);
    return writer;
  }
}
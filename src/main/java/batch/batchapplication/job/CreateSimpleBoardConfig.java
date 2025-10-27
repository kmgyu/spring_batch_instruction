package batch.batchapplication.job;

import batch.batchapplication.board.domain.Board;
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
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CreateSimpleBoardConfig {

  private static final int TOTAL_COUNT = 10_000;  // 생성할 Board 수
  private static final int CHUNK_SIZE  = 100;     // 커밋 단위

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final EntityManagerFactory entityManagerFactory;


  @Bean
  public Job boardCreateJob() {
    return new JobBuilder("boardCreateJob", jobRepository)
            .start(boardStep())
            .build();
  }

  @Bean
  public Step boardStep() {
    return new StepBuilder("boardStep", jobRepository)
            .<Board, Board>chunk(CHUNK_SIZE, transactionManager)
            .reader(boardReader())
            .writer(boardWriter())
            .build();
  }

  @Bean
  public AbstractItemCountingItemStreamItemReader<Board> boardReader() {
    return new AbstractItemCountingItemStreamItemReader<Board>() {
      private Integer count = 0;

      {
        setName("userReader");
      }

      @Override
      protected Board doRead() {
        if (count++ < TOTAL_COUNT) {
          return Board.builder()
                  .title(count.toString())
                  .content(count.toString())
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
  public ItemWriter<Board> boardWriter() {
    JpaItemWriter<Board> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(entityManagerFactory);
    return writer;
  }
}

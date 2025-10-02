package batch.batchapplication.job;

import batch.batchapplication.auth.domain.User;
import batch.batchapplication.auth.domain.UserSnapshot;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.Step;

import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PreheatUsersJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final EntityManagerFactory emf;
  private final RedisTemplate<String, Object> redis;

  @Bean
  @StepScope
  public JpaPagingItemReader<User> userPreheatReader(
          @Value("#{jobParameters['lastId'] ?: 0}") Long lastId
  ) {
    // id 커서 기반 증분
    return new JpaPagingItemReaderBuilder<User>()
            .name("userPreheatReader")
            .entityManagerFactory(emf)
            .queryString("select u from User u where u.id > :lastId order by u.id asc")
            .parameterValues(java.util.Map.of("lastId", lastId))
            .pageSize(500)
            .build();
  }

  @Bean
  public ItemProcessor<User, UserSnapshot> userToSnapshotProcessor() {
    return u -> new UserSnapshot(
            u.getId(),
            u.getUsername(),
            u.getEmail(),
            u.getPassword(),
            List.of() // 현재 권한 없음
    );
  }

  @Bean
  @StepScope
  public ItemWriter<UserSnapshot> userSnapshotRedisWriter(
          @Value("#{jobParameters['ns'] ?: '${app.preheat.namespace}'}") String ns,
          @Value("#{jobParameters['ttlSeconds'] ?: ${app.preheat.ttl-seconds-user}}") Long ttlSeconds
  ) {
    return items -> {
      Duration ttl = Duration.ofSeconds(ttlSeconds);
//      redis.executePipelined((RedisCallback<Object>) con -> {
//        var keySer = redis.getStringSerializer();
//        var valSer = redis.getValueSerializer();
//        for (UserSnapshot s : items) {
//          byte[] k1 = keySer.serialize(ns + ":user:username:" + s.username());
//          byte[] v1 = keySer.serialize(String.valueOf(s.id()));
//          byte[] k2 = keySer.serialize(ns + ":user:" + s.id() + ":snapshot");
//          byte[] v2 = valSer.serialize(s);
//          con.setEx(k1, ttl.getSeconds(), v1);
//          con.setEx(k2, ttl.getSeconds(), v2);
//        }
//        return null;
//      });
      var keySer = (RedisSerializer<String>) redis.getStringSerializer();
      var valSer = (RedisSerializer<Object>) redis.getValueSerializer();

      redis.executePipelined((RedisCallback<Object>) con -> {
        for (UserSnapshot s : items) {
          byte[] k1 = keySer.serialize(ns + ":user:username:" + s.username());
          byte[] v1 = keySer.serialize(String.valueOf(s.id()));
          byte[] k2 = keySer.serialize(ns + ":user:" + s.id() + ":snapshot");
          byte[] v2 = valSer.serialize((Object) s); // 명시적 Object 캐스트로 capture 문제 회피
          con.setEx(k1, ttl.getSeconds(), v1);
          con.setEx(k2, ttl.getSeconds(), v2);
        }
        return null;
      });
    };
  }

  @Bean
  public Step preheatUsersStep(
          JpaPagingItemReader<User> userPreheatReader,
          ItemProcessor<User, UserSnapshot> userToSnapshotProcessor,
          ItemWriter<UserSnapshot> userSnapshotRedisWriter
  ) {
    return new StepBuilder("preheatUsersStep", jobRepository)
            .<User, UserSnapshot>chunk(500, transactionManager)
            .reader(userPreheatReader)
            .processor(userToSnapshotProcessor)
            .writer(userSnapshotRedisWriter)
            .faultTolerant().retryLimit(3).retry(Exception.class)
            .build();
  }

  @Bean
  public Job preheatUsersJob(org.springframework.batch.core.Step preheatUsersStep) {
    return new JobBuilder("preheatUsersJob", jobRepository)
            .start(preheatUsersStep)
            .build();
  }
}
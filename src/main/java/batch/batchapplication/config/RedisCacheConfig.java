package batch.batchapplication.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.time.Duration;


@EnableCaching
@Configuration
public class RedisCacheConfig {

  /**
   * 일반적인 컨텐츠들을 @Cachable 등의 어노테이션을 이용할 때 사용됩니다.
   * aop와 연관되어 캐싱을 수행합니다.
   * @param cf
   * @return
   */
  @Bean
  public CacheManager contentCacheManager(RedisConnectionFactory cf) {
    RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())) // Value Serializer 변경
            .entryTtl(Duration.ofMinutes(3L)); // 캐시 수명 3분

    return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(cf).cacheDefaults(redisCacheConfiguration).build();
  }

// Redis의 경우, 이런 식으로 config를 작성해줄 수 있다.
// 아래와 같은 경우 커스텀 설정을 이용하며 이외의 경우 properties로도 충분히 가능함.
// 특정 직렬화(Serializer) 방식을 사용하고 싶을 때.
// 클러스터(Cluster), 센티넬(Sentinel) 등 복잡한 Redis 구성을 사용할 때 (속성으로도 어느 정도 가능하지만, 더 세밀한 제어를 위해 Configuration이 필요할 수 있습니다).
// 연결 풀(Connection Pool) 설정을 세밀하게 제어하고 싶을 때 (일부 설정은 속성으로 가능).
//  @Bean
//  public RedisConnectionFactory redisConnectionFactory() {
//    return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
//  }
//
//  @Bean
//  public RedisTemplate<String, Object> redisTemplate() {
//    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//
//    redisTemplate.setConnectionFactory(redisConnectionFactory());
//    redisTemplate.setKeySerializer(new StringRedisSerializer());
//    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//    return redisTemplate;
//  }
//
//  @Bean
//  public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory){
//    RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//
//    Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
//
//    return RedisCacheManager.RedisCacheManagerBuilder
//            .fromConnectionFactory(connectionFactory)
//            .cacheDefaults(redisCacheConfiguration)
//            .withInitialCacheConfigurations(redisCacheConfigurationMap)
//            .build();
//  }
}

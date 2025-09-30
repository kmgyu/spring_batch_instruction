package batch.batchapplication.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.*;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

import java.time.Duration;

/**
 * 세션 저장소에 대한 관심사이기 때문에 SecurityConfig와 분리됩니다.
 */
@Configuration
@EnableRedisHttpSession
public class SessionConfig {

  /**
   * 세션 속성 직렬화 방식을 JSON으로 설정합니다.
   * 이 빈이 있으면 Spring Session이 자동으로 이를 사용하여 세션을 직렬화/역직렬화합니다.
   * 기본적ㅇ느로 java serializalizer를 사용하나 다른 어플리케이션과의 호환성을 위해 이것이 권장된다고 합니다.
   */
  // Java 직렬화 사용
  @Bean
  public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
    // Jackson JSON 직렬화 대신, Spring Data Redis의 기본 직렬화(Java Serialization)를 사용합니다.
    return new JdkSerializationRedisSerializer();
  }

//  @Bean
//  public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
////    return new GenericJackson2JsonRedisSerializer(objectMapper());
//
////    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
////    RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
////            .entryTtl(Duration.ofMinutes(5L))
////            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
////            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
////                    new GenericJackson2JsonRedisSerializer(objectMapper())
////            ));
//
//    return new GenericJackson2JsonRedisSerializer(objectMapper());
//  }

  private ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // 1. 필요한 모든 기본 Jackson 모듈 명시적 등록 (컬렉션, 파라미터 이름, Java 8 타입 처리)
    //    -> "authorities"와 같은 setterless 컬렉션 필드 처리 능력 향상
    mapper.registerModules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());

    // 2. Spring Security 모듈 명시적 등록 (UsernamePasswordAuthenticationToken 등의 생성자 문제 해결)
    mapper.registerModules(SecurityJackson2Modules.getModules(AbstractHttpSessionApplicationInitializer.class.getClassLoader()));

    // 2. 사용자 정의 클래스(Principal)의 Allowlist 문제를 해결하는 Default Typing 활성화
    mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
    );
    return mapper;
  }
}

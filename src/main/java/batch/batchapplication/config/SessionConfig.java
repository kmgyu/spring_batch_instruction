package batch.batchapplication.config;

import batch.batchapplication.auth.domain.User;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
   * 기본적으로 java serializalizer를 사용하나 다른 어플리케이션과의 호환성을 위해 이것이 권장된다고 합니다.
   */
  // Java 기본 직렬화 사용
//  @Bean
//  public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
//    // Jackson JSON 직렬화 대신, Spring Data Redis의 기본 직렬화(Java Serialization)를 사용합니다.
//    return new JdkSerializationRedisSerializer();
//  }

//  지피티의 제안

  @Bean
  public RedisSerializer<Object> springSessionDefaultRedisSerializer(ObjectMapper mapper) {
    // 1) Spring Security 모듈 등록 (Authentication, GrantedAuthority 등 직렬화 지원)
    mapper.registerModules(
            SecurityJackson2Modules.getModules(getClass().getClassLoader())
    );

    // 2) principal로 들어갈 '네 엔티티'를 allowlist에 추가 (서브타입 등록)
    mapper.registerSubtypes(
            new NamedType(
                    User.class,
                    "batch.batchapplication.auth.domain.User"
            )
    );

    // 3) 필요 시 Mixin으로 타입정보 힌트 (principal이 Object로 취급되므로)
    mapper.addMixIn(batch.batchapplication.auth.domain.User.class, PrincipalTypeHint.class);

    // 기본 모듈(Jdk8/JavaTime 등)은 부트가 이미 넣어준 mapper에 있음
    return new GenericJackson2JsonRedisSerializer(mapper);
  }

  // principal 엔티티에 직접 애노테이션을 못 붙인다면 Mixin으로 타입 정보를 부여
  abstract static class PrincipalTypeHint {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class"
    )
    // 필드가 아니라 타입 레벨 힌트만 주면 됨 (빈 클래스)
    static class Mix {}
  }
}

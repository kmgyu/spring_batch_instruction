package batch.batchapplication.config;

import batch.batchapplication.auth.domain.User;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.*;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 세션 저장소에 대한 관심사이기 때문에 SecurityConfig와 분리됩니다.
 * 현재 비활성화
 */
@Configuration
@EnableRedisHttpSession
public class SessionConfig {

  /**
   * 세션 속성 직렬화 방식을 JSON으로 설정합니다.
   * 이 빈(springSessionDefaultRedisSerializer)이 있으면 Spring Session이 자동으로 이를 사용하여 세션을 직렬화/역직렬화합니다.
   * 일급 함수 자동 설정 지원으로 인해 별다른 설정 없이도 거의-모든 것이 지원됩니다. 항상 감사하십시오. (레퍼런스 : https://docs.spring.io/spring-session/reference/guides/boot-redis.html)
   * 기본적으로 java serializalizer를 사용하나 다른 어플리케이션과의 호환성을 위해 이것이 권장된다고 합니다.
   */

  // Java 기본 직렬화 사용 (안해도... 무관...? 기본 세팅입니다.)
//  @Bean
//  public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
//    // Jackson JSON 직렬화 대신, Spring Data Redis의 기본 직렬화(Java Serialization)를 사용합니다.
//    return new JdkSerializationRedisSerializer();
//  }

  /**
   * @Class를 세션 저장소의 필드에서 제거하는 것은 실패하였습니다.
   * 직렬화 과정에서 거의 필수로 사용되기 때문인 것으로 보이며, 이것을 수정하려면 더욱 로우레벨로 접근해야할 것 같습니다.
   * @param mapper
   * @return
   */
  @Bean
  public RedisSerializer<Object> springSessionDefaultRedisSerializer(ObjectMapper mapper) {
    // 1. Spring Security 모듈 등록 (Authentication, GrantedAuthority 등 직렬화 지원)
    // Spring Security 객체들이 역직렬화 시 Type Id를 사용하도록 설정됩니다.
    mapper.registerModules(
            SecurityJackson2Modules.getModules(getClass().getClassLoader())
    );

    // 2. Principal 객체에 대한 안전한 Type Info Mixin 적용
    // Principal(UserDetails)이 Authentication 객체 내부에 Object 타입으로 저장되므로,
    // 역직렬화 시 Jackson이 실제 클래스를 알 수 있도록 명시적인 힌트가 필요합니다.
    mapper.addMixIn(Object.class, PrincipalTypeHint.class);

    // 3. Principal DTO 클래스를 서브타입으로 등록 (Whitelisting 및 별칭 부여)
    // UserSessionDTO 클래스를 "UserSession" 별칭으로 명시적으로 등록합니다.
    // 이는 RCE 공격자가 임의의 클래스명을 삽입하는 것을 방지하는 Whitelisting 효과를 줍니다.
    mapper.registerSubtypes(
            new NamedType( User.class, "UserSession" )
    );

    // 최종적으로, Serializer를 반환합니다.
    return new GenericJackson2JsonRedisSerializer(mapper);
  }

  /**
   * Principal 객체에 대한 역직렬화 힌트를 제공하는 Mixin 인터페이스입니다.
   * Object.class에 적용되어 Authentication 내부의 Principal 필드에 영향을 줍니다.
   * use = JsonTypeInfo.Id.CLASS 대신 use = JsonTypeInfo.Id.NAME을 사용하여 RCE를 방지하고,
   * 별칭(Name)을 통해 역직렬화를 유도합니다.
   */
  abstract static class PrincipalTypeHint {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class"
    )
    // 필드가 아니라 타입 레벨 힌트만 주면 됨 (빈 클래스)
    static class Mix {}
  }
}

package batch.batchapplication.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "redis.cluster")
public class RedisConfig {
  private String nodes;  // "ip1:port,ip2:port,ip3:port"
  private int maxRedirects;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    // 1) 콤마로 분리해서 각 노드를 리스트로 변환
    List<String> nodeList = Arrays.stream(nodes.split(","))
            .map(String::trim)
            .toList();

    // 2) Redis 클러스터 설정 생성
    RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(nodeList);
    clusterConfig.setMaxRedirects(maxRedirects);

    // 3) 커넥션 팩토리 반환
    return new LettuceConnectionFactory(clusterConfig);
  }


  // todo: 직렬화 취약점 보완 필요. json 직렬화 필요함.
//  @Bean
//  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//    redisTemplate.setConnectionFactory(redisConnectionFactory);
//    redisTemplate.setKeySerializer(new StringRedisSerializer());
//    redisTemplate.setValueSerializer(new StringRedisSerializer());
//    return redisTemplate;
//  }
}
package batch.batchapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  LettuceConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
  }

  @Bean
  RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory cf) {
    var tpl = new RedisTemplate<String, Object>();
    var str = new StringRedisSerializer();
    var json = new GenericJackson2JsonRedisSerializer();

    tpl.setConnectionFactory(cf);
    tpl.setKeySerializer(str);
    tpl.setHashKeySerializer(str);
    tpl.setValueSerializer(json);
    tpl.setHashValueSerializer(json);
    tpl.afterPropertiesSet();
    return tpl;
  }
}

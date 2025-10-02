package batch.batchapplication.auth.service;


import batch.batchapplication.auth.domain.User;
import batch.batchapplication.auth.domain.UserRepository;
import batch.batchapplication.auth.domain.UserSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedAuthUserDetailService implements UserDetailsService {

  private final UserRepository userRepository;
  private final RedisTemplate<String, Object> redis;

  @Value("${app.preheat.namespace:preheat:v1}")
  private String ns;

  @Value("${app.preheat.ttl-seconds-user:86400}")
  private long ttlSeconds;

  // Redis key transfer
  // :는 단순히 구분자 역할을 해준다.
  private String kUserIdByUsername(String username) { return ns + ":user:username:" + username; }
  private String kUserSnapshotById(Long id) { return ns + ":user:" + id + ":snapshot"; }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 1) username -> id
    String idStr = ((Integer) redis.opsForValue().get(kUserIdByUsername(username))).toString();
    if (idStr != null) {
      Long id = Long.valueOf(idStr);
      var snap = (UserSnapshot) redis.opsForValue().get(kUserSnapshotById(id));
      if (snap != null) {
        log.debug("User cache HIT: {}", username);
        return toSpringUser(snap);
      }
    }
    // 2) MISS → DB
    log.debug("User cache MISS: {}", username);
    User u = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    // 현재 도메인에 권한 컬렉션이 비어 있으므로 빈 리스트 유지
    List<String> auths = List.of();

    var snap = new UserSnapshot(u.getId(), u.getUsername(), u.getEmail(), u.getPassword(), auths);

    // 3) write-through 업서트
    Duration ttl = Duration.ofSeconds(ttlSeconds);
    redis.opsForValue().set(kUserIdByUsername(u.getUsername()), String.valueOf(u.getId()), ttl);
    redis.opsForValue().set(kUserSnapshotById(u.getId()), snap, ttl);

    return toSpringUser(snap);
  }

  private UserDetails toSpringUser(UserSnapshot s) {
    var granted = s.authorities().stream().map(SimpleGrantedAuthority::new).toList();
    return org.springframework.security.core.userdetails.User.withUsername(s.username())
            .password(s.passwordHash() == null ? "{noop}" : s.passwordHash())
            .authorities(granted)
            .accountExpired(false).accountLocked(false)
            .credentialsExpired(false).disabled(false)
            .build();
  }
}
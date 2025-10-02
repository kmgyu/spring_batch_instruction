package batch.batchapplication.auth.service;

import batch.batchapplication.auth.domain.User;
import batch.batchapplication.auth.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUserDetailService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("Spring Security loadUserByUsername 호출: username={}", username);

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
              log.warn("Spring Security - 사용자 없음: username={}", username);
              return new UsernameNotFoundException("User not found: " + username);
            });

    log.info("Spring Security - 사용자 로드 성공: username={}, authorities={}",
            username, user.getAuthorities());
    return user;
  }
}

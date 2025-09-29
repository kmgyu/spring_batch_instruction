package batch.batchapplication.auth.service;

import batch.batchapplication.auth.domain.User;
import batch.batchapplication.auth.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthUserDetailService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//    log.debug("Spring Security loadUserByUsername 호출: email={}", email);

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
//              log.warn("Spring Security - 사용자 없음: email={}", email);
              return new UsernameNotFoundException("User not found: " + email);
            });

//    log.debug("Spring Security - 사용자 로드 성공: email={}, authorities={}",
//            email, user.getAuthorities());
    return user;
  }
}

package batch.batchapplication.auth.service;

import batch.batchapplication.auth.domain.User;
import batch.batchapplication.auth.domain.UserRepository;
import batch.batchapplication.auth.dto.SignupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User signup(SignupDTO signupDto) {
//    log.info("회원가입 시도: email={}", signupDto.getEmail());

//    // 이메일 중복 검사 (안함)
//    if (userRepository.existsByEmail(signupDto.getEmail())) {
//      log.warn("회원가입 실패 - 이메일 중복: email={}", signupDto.getEmail());
//      throw new DuplicateEmailException(signupDto.getEmail());
//    }

//    // 비밀번호 확인 검사
//    if (!signupDto.getPassword().equals(signupDto.getPasswordConfirm())) {
//      log.warn("회원가입 실패 - 비밀번호 불일치: email={}", signupDto.getEmail());
//      throw new IllegalArgumentException("Password mismatch");
//    }


    // 사용자 엔티티 생성
    User user = new User();
    user.setEmail(signupDto.getEmail());
    String encodedPassword = passwordEncoder.encode(signupDto.getPassword());
    user.setPassword(encodedPassword);

    User savedUser = userRepository.save(user);
//    log.info("회원가입 성공: email={}, userId={}", signupDto.getEmail(), savedUser.getUserSeq());
    return savedUser;
  }
}

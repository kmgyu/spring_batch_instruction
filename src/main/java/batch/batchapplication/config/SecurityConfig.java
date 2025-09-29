package batch.batchapplication.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/", "/posts", "/auth/signup", "/auth/login",
                                    "/static/**", "/css/**", "/js/**", "/images/**", "/favicon.*").permitAll()  // 정적 리소스 접근 허용
//                        .requestMatchers("/", "/posts", "/auth/signup", "/auth/login", "/favicon.ico").permitAll()
//                            .requestMatchers("/posts/[0-9]+").permitAll()  // regex로 숫자로 된 게시글 조회만 허용
                            .requestMatchers("/**").authenticated()
                            .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/auth/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", true)  // true로 설정하여 강제 리다이렉트
                    .successHandler((request, response, authentication) -> {
                      log.info("로그인 성공: user={}, authorities={}",
                              authentication.getName(), authentication.getAuthorities());
                      response.sendRedirect("/");
                    })
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/auth/logout")
                    .logoutSuccessUrl("/")
                    .permitAll()
            );

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

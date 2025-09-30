package batch.batchapplication.auth;

import batch.batchapplication.auth.dto.LoginDTO;
import batch.batchapplication.auth.dto.SignupDTO;
import batch.batchapplication.auth.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
  private final AuthUserService authUserService;

  @GetMapping("/signup")
  public String signup(Model model) {
    model.addAttribute("signupDTO", new SignupDTO());
    return "auth/signup";
  }

  @PostMapping("/signup")
  public String signup(@ModelAttribute SignupDTO signupDTO) {
    authUserService.signup(signupDTO);
    return "redirect:/";
  }

  @GetMapping("/login")
  public String loginForm(Model model) {
    log.debug("로그인 폼 요청");
    model.addAttribute("loginDTO", new LoginDTO());
    return "auth/login";
  }
}

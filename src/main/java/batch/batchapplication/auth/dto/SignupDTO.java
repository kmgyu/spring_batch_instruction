package batch.batchapplication.auth.dto;

import lombok.Data;

@Data
public class SignupDTO {
  String username;

  String email;

  String password;
}

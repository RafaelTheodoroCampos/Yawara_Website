package br.com.yamarasolution.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.yamarasolution.config.MailConfig;
import br.com.yamarasolution.model.User;
import br.com.yamarasolution.repository.UserRepository;
import br.com.yamarasolution.utils.JwtUtils;
import jakarta.transaction.Transactional;

@Service
public class PasswordRecoveryService {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserRepository repository;

  @Autowired
  private PasswordEncoder encoder;

  @Autowired
  private MailConfig mailConfig;

  /**
   * If the email exists in the database, generate a token and send it to the user
   * 
   * @param email the email of the user
   */
  public void sendRecoveryEmail(String email) {

    if (repository.existsByEmailIgnoreCase(email)) {
      String token = jwtUtils.generateTokenRecovery(email);
      String recoveryLink = "http://localhost:8080/password-recovery/reset?token=" + token;
      mailConfig.sendEmail(email, "Password Recovery", recoveryLink);
    }
  }

  /**
   * If the token is valid, then the password is reset
   * 
   * @param token    The token that was sent to the user's email address.
   * @param password The new password
   * @return Boolean
   */
  @Transactional
  public Boolean resetPassword(String token, String password) {
    String email = jwtUtils.getSubjectFromToken(token);
    if (email != null && jwtUtils.validateJwtToken(token)) {
      User user = repository.findByEmail(email);
      if (user != null) {
        user.setPassword(encoder.encode(password));
        repository.save(user);
        return true;
      }
      return false;
    } else {
      return false;
    }
  }

}

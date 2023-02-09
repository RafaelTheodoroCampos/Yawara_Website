package br.com.yawarasolution.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.yawarasolution.exception.ApiError;
import br.com.yawarasolution.service.PasswordRecoveryService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/password-recovery")
@Tag(name = "Password Recovery", description = "Recovery password")
public class PasswordRecoveryController {

  @Autowired
  private PasswordRecoveryService service;

  /**
   * If an account exists the email has been sent
   * 
   * @param email The email address of the user who wants to recover their
   *              password.
   * @return A ResponseEntity with a 200 status code and a message.
   */
  @PostMapping("/send-email")
  @Operation(summary = "Send email recovery", description = "Password Recovery", responses = {
      @ApiResponse(responseCode = "200", description = "If an account exists the email has been sent"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<?> sendRecoveryEmail(@Valid @RequestParam @Email String email) {
    service.sendRecoveryEmail(email);
    return ResponseEntity.ok("If an account exists the email has been sent");
  }

  /**
   * The function takes in a token and a password, and if the token is valid, it
   * will reset the password
   * 
   * @param token    The token that was sent to the user's email address
   * @param password The new password
   * @return A ResponseEntity object.
   */
  @PostMapping("/reset")
  @Operation(summary = "Password Recovery", description = "Password Recovery", responses = {
      @ApiResponse(responseCode = "200", description = "Password reset"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> resetPassword(@Valid @RequestParam String token,
      @RequestParam @NotNull @Size(min = 6, max= 40) String password) {
    try {
      if (service.resetPassword(token, password)) {
        return ResponseEntity.ok().body("Password reset successfully");
      }
      return ResponseEntity.badRequest().body("Invalid password reset token");
    } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException
        | IllegalArgumentException e) {
          return ResponseEntity.unprocessableEntity().body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }
}

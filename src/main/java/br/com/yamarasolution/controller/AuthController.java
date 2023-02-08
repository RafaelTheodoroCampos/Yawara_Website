package br.com.yamarasolution.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.yamarasolution.DTO.auth.LoginRequest;
import br.com.yamarasolution.DTO.auth.RefreshTokenRequest;
import br.com.yamarasolution.DTO.auth.RoleRequest;
import br.com.yamarasolution.DTO.auth.SignupRegisterResponse;
import br.com.yamarasolution.DTO.auth.SignupRequest;
import br.com.yamarasolution.DTO.auth.SignupResponse;
import br.com.yamarasolution.DTO.auth.TokenRefreshResponse;
import br.com.yamarasolution.exception.AccountExcpetion;
import br.com.yamarasolution.exception.ApiError;
import br.com.yamarasolution.exception.TokenRefreshException;
import br.com.yamarasolution.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth", description = "Autentificação de usuario")
public class AuthController {

  @Autowired
  private AuthService authService;

  /**
   * This function is used to authenticate the user and return the access token
   * 
   * @param loginRequest This is the request body that is sent to the server.
   * @return ResponseEntity.ok().header("Authorization",
   *         signupResponse.getAccessToken()).body(signupResponse);
   */
  @PostMapping("/signin")
  @Operation(summary = "Sign In Service", description = "Sign In Service", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully Singned In!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignupResponse.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      SignupResponse signupResponse = authService.authenticateUser(loginRequest);
      return ResponseEntity.ok().header("Authorization", signupResponse.getAccessToken()).body(signupResponse);
    } catch (AccountExcpetion e) {
      return ResponseEntity.unprocessableEntity().body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * This function is used to register a user in the system
   * 
   * @param signUpRequest This is the request body that is sent to the server.
   */
  @PostMapping("/signup")
  @Operation(summary = "Register In Service", description = "register In Service", responses = {
      @ApiResponse(responseCode = "201", description = "Successfully Register In!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignupRegisterResponse.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    try {
      SignupRegisterResponse response = authService.registerUser(signUpRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (RuntimeException e) {
      return ResponseEntity.unprocessableEntity().body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * This function is responsible for adding a new role to the user
   * 
   * @param rolesIn   is the object that comes in the request body
   * @param idUsuario UUID
   * @return The response is a list of roles that the user has.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/new-roles/{idUsuario}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Add news Roles", description = "add news Roles, Admin Only", responses = {
      @ApiResponse(responseCode = "201", description = "Roles Register In!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignupRegisterResponse.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> newRoles(@Valid @RequestBody RoleRequest rolesIn, @PathVariable UUID idUsuario) {
    try {
      SignupRegisterResponse response = authService.newRoles(rolesIn, idUsuario);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (AccountExcpetion e) {
      return ResponseEntity.unprocessableEntity().body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * It removes the roles of a user.
   * 
   * @param rolesIn   is a list of roles that I want to remove from the user
   * @param idUsuario UUID
   * @return The response is a list of roles that the user has.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/delete-roles/{idUsuario}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Remove news Roles", description = "Remove news Roles, Admin only", responses = {
      @ApiResponse(responseCode = "201", description = "Roles Register In!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignupRegisterResponse.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> removeRoles(@Valid @RequestBody RoleRequest rolesIn, @PathVariable UUID idUsuario) {
    try {
      SignupRegisterResponse response = authService.removeRoles(rolesIn, idUsuario);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (AccountExcpetion e) {
      return ResponseEntity.unprocessableEntity().body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * It takes a code as a parameter and returns a response entity with a status
   * code of 201 and a body of
   * the response from the authService.confirmAccount(code) function
   * 
   * @param code The code that was sent to the user's email address.
   * @return ResponseEntity.badRequest().body(e.getMessage());
   */
  @PutMapping("/confirm-account")
  @Operation(summary = "Confirm Account", description = "Confirm Account", responses = {
      @ApiResponse(responseCode = "201", description = "Account activated successfully!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> confirmAccount(@RequestParam("code") String code) {
    try {
      String response = authService.confirmAccount(code);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (AccountExcpetion e) {
      return ResponseEntity.unprocessableEntity().body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * It takes a request body, validates it, and then passes it to the
   * authService.refreshtoken() function
   * 
   * @param request The request object that will be passed to the controller
   *                method.
   * @return ResponseEntity.status(HttpStatus.CREATED).body(response);
   */
  @PostMapping("/refreshtoken")
  @Operation(summary = "Refresh Token", description = "Refresh Token", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully Refresh Token!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefreshTokenRequest.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> refreshtoken(@Valid @RequestBody RefreshTokenRequest request) {
    try {
      TokenRefreshResponse response = authService.refreshtoken(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (TokenRefreshException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
  }

  /**
   * Signout In Service
   * 
   * @return A ResponseEntity with a String body.
   */
  @PostMapping("/signout")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Signout In Service", description = "Signout In Service", responses = {
      @ApiResponse(responseCode = "200", description = "Log out successful!", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<String> logoutUser() {
    return ResponseEntity.ok(authService.logoutUser());
  }
}

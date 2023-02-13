package br.com.yawarasolution.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.yawarasolution.DTO.auth.LoginRequest;
import br.com.yawarasolution.DTO.auth.RefreshTokenRequest;
import br.com.yawarasolution.DTO.auth.RoleRequest;
import br.com.yawarasolution.DTO.auth.SignupRegisterResponse;
import br.com.yawarasolution.DTO.auth.SignupRequest;
import br.com.yawarasolution.DTO.auth.SignupResponse;
import br.com.yawarasolution.DTO.auth.TokenRefreshResponse;
import br.com.yawarasolution.config.MailConfig;
import br.com.yawarasolution.enums.ERole;
import br.com.yawarasolution.exception.AccountException;
import br.com.yawarasolution.exception.TokenRefreshException;
import br.com.yawarasolution.exception.UserException;
import br.com.yawarasolution.model.RefreshToken;
import br.com.yawarasolution.model.Role;
import br.com.yawarasolution.model.User;
import br.com.yawarasolution.repository.EmailChangeRequestRepository;
import br.com.yawarasolution.repository.RoleRepository;
import br.com.yawarasolution.repository.UserRepository;
import br.com.yawarasolution.utils.JwtUtils;
import jakarta.transaction.Transactional;

@Service
public class AuthService {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder encoder;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private MailConfig mailConfig;

  @Autowired
  private EmailChangeRequestRepository emailChangeRequestRepository;

  /**
   * Update the last login time of the user with the given id.
   * 
   * @param id The id of the user
   */
  @Transactional
  public void lastLogin(UUID id) {
    Optional<User> user = userRepository.findById(id);
    if (!user.isPresent()) {
      throw new AccountException("User not found");
    }
    user.get().setLastLogin(Instant.now());
    userRepository.save(user.get());
  }

  /**
   * It takes a username and password, checks if the user exists, and if so, it
   * returns a JWT token
   * 
   * @param loginRequest username and password
   * @return A SignupResponse object.
   */
  @Transactional
  public SignupResponse authenticateUser(LoginRequest loginRequest) {
    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    String jwt = jwtUtils.generateJwtToken(userDetails);

    List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
        .collect(Collectors.toList());

    RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

    lastLogin(userDetails.getId());

    return new SignupResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getUsername(),
        userDetails.getEmail(), roles);
  }

  /**
   * The function takes a SignupRequest object as a parameter, checks if the
   * username and email are
   * already taken, creates a new user, adds a role to the user, generates an
   * activation code, saves
   * the user to the database, creates a list of roles, creates a message, and
   * sends the message to the
   * user's email
   * 
   * @param signUpRequest the request body from the frontend
   * @return A new SignupRegisterResponse object is being returned.
   */
  @Transactional
  public SignupRegisterResponse registerUser(SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      throw new AccountException("Error: Username is already taken!");
    }

    if (userRepository.existsByEmailIgnoreCase(signUpRequest.getEmail())) {
      throw new AccountException("Error: Email is already in use!");
    }

    emailChangeRequestRepository.findByNewEmailIgnoreCaseAndConfirmedFalse(signUpRequest.getEmail())
        .ifPresent(request -> {
          throw new UserException("Error: Email is already in use!");
        });

    // Create new user's account
    User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
        encoder.encode(signUpRequest.getPassword()), false, null, Instant.now(), null,
        "https://cdn.pixabay.com/photo/2016/04/01/11/25/avatar-1300331_960_720.png", signUpRequest.getTelefone(),
        signUpRequest.getName());

    Set<Role> roles = new HashSet<>();
    String activationCode = UUID.randomUUID().toString();

    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    roles.add(userRole);

    user.setRoles(roles);
    user.setActivationCode(activationCode);
    userRepository.save(user);

    List<ERole> rolesList = roles.stream().map(Role::getName).collect(Collectors.toList());

    String message = "Hello " + user.getUsername() + ",\n" +
        "Please confirm your account by clicking the following link: \n" +
        "http://localhost:8080/api/auth/confirm-account?code=" + activationCode + "\n" +
        "or by entering the activation code: " + activationCode + "\n" +
        "Thank you for registering!";

    // Use JavaMailSender to send the email
    mailConfig.sendEmail(user.getEmail(), "Confirm your account", message);

    return new SignupRegisterResponse(user, rolesList);
  }

  /**
   * It takes a user id and a list of roles, and adds the roles to the user
   * 
   * @param rolesIn   is a list of roles that I want to add to the user
   * @param idUsuario UUID
   * @return A SignupRegisterResponse object.
   */
  @Transactional
  public SignupRegisterResponse newRoles(RoleRequest rolesIn, UUID idUsuario) {
    Optional<User> user = userRepository.findById(idUsuario);

    if (!user.isPresent()) {
      throw new AccountException("Error: User notFound");
    }

    Set<String> strRoles = rolesIn.getRoles();
    Set<Role> roles = new HashSet<>();

    for (String role : strRoles) {
      ERole eRole;
      switch (role) {
        case "admin":
          eRole = ERole.ROLE_ADMIN;
          break;
        default:
          eRole = ERole.ROLE_USER;
      }
      Role foundRole = roleRepository.findByName(eRole)
          .orElseThrow(() -> new AccountException("Error: Role is not found."));
      roles.add(foundRole);
    }

    Set<Role> currentRoles = user.get().getRoles();
    currentRoles.addAll(roles);
    user.get().setRoles(currentRoles);
    userRepository.save(user.get());

    List<ERole> rolesList = currentRoles.stream().map(Role::getName).collect(Collectors.toList());

    return new SignupRegisterResponse(user.get(), rolesList);
  }

  /**
   * It removes the roles from the user
   * 
   * @param rolesIn   is a list of roles that I want to remove from the user
   * @param idUsuario UUID
   * @return A SignupRegisterResponse object.
   */
  @Transactional
  public SignupRegisterResponse removeRoles(RoleRequest rolesIn, UUID idUsuario) {
    Optional<User> user = userRepository.findById(idUsuario);

    if (!user.isPresent()) {
      throw new AccountException("Error: User notFound");
    }

    Set<String> strRoles = rolesIn.getRoles();
    Set<Role> roles = new HashSet<>();

    for (String role : strRoles) {
      ERole eRole;
      switch (role) {
        case "admin":
          eRole = ERole.ROLE_ADMIN;
          break;
        default:
          eRole = null;
      }
      Role foundRole = roleRepository.findByName(eRole)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(foundRole);
    }

    Set<Role> currentRoles = user.get().getRoles();
    for (Role role : roles) {
      if (role.getName().equals(ERole.ROLE_USER) && !currentRoles.contains(role)) {
        throw new AccountException("Error: ROLE_USER cannot be removed");
      }
    }

    currentRoles.removeAll(roles);
    user.get().setRoles(currentRoles);
    userRepository.save(user.get());

    List<ERole> rolesList = currentRoles.stream().map(Role::getName).collect(Collectors.toList());

    return new SignupRegisterResponse(user.get(), rolesList);
  }

  /**
   * It takes a string, finds a user with that string as an activation code, sets
   * the user's isActive
   * field to true, sets the user's activationCode field to null, and saves the
   * user
   * 
   * @param code The activation code that was sent to the user's email address.
   * @return A string
   */
  @Transactional
  public String confirmAccount(String code) {
    User user = userRepository.findByActivationCode(code)
        .orElseThrow(() -> new AccountException("User activationCode " + code + " is not a valid activation code"));

    if (user == null) {
      throw new AccountException("Error: Invalid activation code.");
    }

    user.setIsActive(true);
    user.setActivationCode(null);
    userRepository.save(user);

    return "Account activated successfully!";
  }

  /**
   * It takes a refresh token, finds the user associated with it, generates a new
   * token, and returns it
   * 
   * @param request The request object that contains the refresh token.
   * @return A new token and a new refresh token.
   */
  @Transactional
  public TokenRefreshResponse refreshtoken(RefreshTokenRequest request) {
    String requestRefreshToken = request.getRefreshToken();

    return refreshTokenService.findByToken(requestRefreshToken)
        .map(refreshTokenService::verifyExpiration)
        .map(RefreshToken::getUser)
        .map(user -> {
          if (!user.getIsActive()) {
            refreshTokenService.deleteByUserId(user.getId());
            throw new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!");
          }
          String token = jwtUtils.generateTokenFromUsername(user.getUsername(), user.getId());
          List<Role> roles = user.getRoles().stream().collect(Collectors.toList());
          List<ERole> rolesList = roles.stream().map(Role::getName).collect(Collectors.toList());
          return new TokenRefreshResponse(token, requestRefreshToken, user.getId(),
              user.getUsername(), user.getEmail(), rolesList);
        })
        .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
            "Refresh token is not in database!"));
  }

  /**
   * It deletes the refresh token from the database
   * 
   * @return A string.
   */
  @Transactional
  public String logoutUser() {
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    UUID userId = userDetails.getId();
    refreshTokenService.deleteByUserId(userId);
    return "Log out successful!";
  }

}

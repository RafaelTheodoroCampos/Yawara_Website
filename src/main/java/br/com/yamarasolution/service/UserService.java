package br.com.yamarasolution.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.yamarasolution.DTO.users.EmailChangeRequestDTO;
import br.com.yamarasolution.DTO.users.UserRequestAdminDTO;
import br.com.yamarasolution.DTO.users.UserRequestDTO;
import br.com.yamarasolution.DTO.users.UserResponseDTO;
import br.com.yamarasolution.config.MailConfig;
import br.com.yamarasolution.exception.CategoryException;
import br.com.yamarasolution.exception.UserException;
import br.com.yamarasolution.model.EmailChangeRequest;
import br.com.yamarasolution.model.User;
import br.com.yamarasolution.repository.EmailChangeRequestRepository;
import br.com.yamarasolution.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder encoder;

  @Autowired
  private MailConfig mailConfig;

  @Autowired
  private EmailChangeRequestRepository emailChangeRequestRepository;

  @Autowired
  private FirebaseFileService firebaseFileService;

  /**
   * It gets the user from the database and returns it as a UserResponseDTO
   * 
   * @return UserResponseDTO
   */
  public UserResponseDTO findLoggedUser() {
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    return userRepository.findById(userDetails.getId()).map(UserResponseDTO::new)
        .orElseThrow(() -> new UserException("Could not find user"));
  }

  /**
   * Find all users, map them to a UserResponseDTO, and return a list of
   * UserResponseDTOs.
   * 
   * @return A list of UserResponseDTO objects.
   */
  public List<UserResponseDTO> findAllUsers() {
    return userRepository.findAll().stream()
        .map(UserResponseDTO::new).collect(Collectors.toList());
  }

  /**
   * Find a user by id, if it exists, return a new UserResponseDTO, otherwise
   * throw a UserException.
   * 
   * @param id The id of the user to be found
   * @return A UserResponseDTO object.
   */
  public UserResponseDTO findUserById(UUID id) {
    return userRepository.findById(id).map(UserResponseDTO::new)
        .orElseThrow(() -> new UserException("Could not find user, id= " + id));
  }

  /**
   * It takes an email address as a parameter, finds the user with that email
   * address, and returns a
   * UserResponseDTO object
   * 
   * @param email the email of the user you want to find
   * @return A UserResponseDTO object.
   */
  public UserResponseDTO findUserByEmail(String email) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new CategoryException("Could not find user, email= " + email);
    }
    return new UserResponseDTO(user);
  }

  /**
   * It takes a username as a parameter, finds the user in the database, and
   * returns a UserResponseDTO
   * object
   * 
   * @param username The username of the user to be found.
   * @return UserResponseDTO
   */
  public UserResponseDTO findUserByUsername(String username) {
    return userRepository.findByUsername(username).map(UserResponseDTO::new)
        .orElseThrow(() -> new UserException("Could not find user, username= " + username));
  }

  /**
   * It updates a user's username, email, imageUrl, and password
   * 
   * @param id          UUID
   * @param userRequest
   * @return A UserResponseDTO object
   */
  @Transactional
  public UserResponseDTO updateUserAdmin(UUID id, UserRequestAdminDTO userRequest) {

    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserException("Could not find user id= " + id));

    String username = userRequest.getUsername();
    String email = userRequest.getEmail();

    if (!user.getUsername().equalsIgnoreCase(username) && userRepository.existsByUsername(username)) {
      throw new UserException("Username already exists for User, username= " + username);
    }

    emailChangeRequestRepository.findByNewEmailIgnoreCaseAndConfirmedFalse(email)
        .ifPresent(request -> {
          throw new UserException(email);
        });

    if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
      throw new UserException("Email already exists for User, email= " + email);
    }

    user.setUsername(username);
    user.setEmail(email);
    user.setImageUrl(userRequest.getImageUrl());
    user.setPassword(encoder.encode(userRequest.getPassword()));
    user.setUpdatedAt(Instant.now());
    user.setTelefone(userRequest.getTelefone());
    user = userRepository.save(user);

    return new UserResponseDTO(user);

  }

  /**
   * It updates the user's credentials and sends an email to the user's email
   * address
   * 
   * @param userRequest the object that contains the new username and password
   * @return A UserResponseDTO object
   */
  @Transactional
  public UserResponseDTO updateUser(UserRequestDTO userRequest) {

    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();

    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new UserException("Could not find user id= " + userDetails.getId()));

    String username = userRequest.getUsername();

    if (!user.getUsername().equalsIgnoreCase(username) && userRepository.existsByUsername(username)) {
      throw new UserException("Username already exists for User, username= " + username);
    }

    user.setUsername(username);
    user.setPassword(encoder.encode(userRequest.getPassword()));
    user.setUpdatedAt(Instant.now());
    user.setTelefone(userRequest.getTelefone());
    user = userRepository.save(user);

    String message = "Attention! Your account information has been changed. Please contact us if these changes were not made by you.";

    mailConfig.sendEmail(user.getEmail(), "Credentials update", message);

    return new UserResponseDTO(user);

  }

  /**
   * It sends an email to the user with a confirmation code, and saves the
   * confirmation code in the
   * database
   * 
   * @param emailChangeRequest The email change request object that contains the
   *                           new email.
   */
  @Transactional
  public void updateUserEmail(EmailChangeRequestDTO emailChangeRequest) {
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new UserException("Could not find user id= " + userDetails.getId()));

    if (userRepository.existsByEmailIgnoreCase(emailChangeRequest.getEmail())) {
      throw new UserException("Error: Email is already in use!");
    }

    // Checking if the user has already requested a change of email.
    emailChangeRequestRepository.findByNewEmailIgnoreCaseAndConfirmedFalse(emailChangeRequest.getEmail())
        .ifPresent(request -> {
          throw new UserException(emailChangeRequest.getEmail());
        });

    String confirmationCode = UUID.randomUUID().toString();
    EmailChangeRequest emailChange = new EmailChangeRequest();
    emailChange.setConfirmationCode(confirmationCode);
    emailChange.setNewEmail(emailChangeRequest.getEmail());
    emailChange.setConfirmed(false);
    emailChange.setUser(user);
    emailChange = emailChangeRequestRepository.save(emailChange);

    String message = "Hello " + user.getUsername() + ",\n" +
        "Please confirm your email by clicking the following link: \n" +
        "http://localhost:8080/api/update/email/confirm?code=" + confirmationCode + "\n" +
        "or by entering the activation code: " + confirmationCode;

    // Use JavaMailSender to send the email
    mailConfig.sendEmail(emailChangeRequest.getEmail(), "Confirm your Email", message);
  }

  /**
   * It takes a code, finds the email change request with that code, sets the
   * email change request to
   * confirmed, sets the confirmation code to null, saves the email change
   * request, sets the user's email
   * to the new email, sets the user's updated at to now, and saves the user
   * 
   * @param code The confirmation code that was sent to the user's email address.
   * @return A string
   */
  @Transactional
  public String confirmEmailChangeRequest(String code) {
    EmailChangeRequest emailChange = emailChangeRequestRepository.findByConfirmationCode(code)
        .orElseThrow(() -> new UserException("Email activationCode " + code + " is not a valid activation code"));

    if (emailChange == null) {
      throw new UserException("Error: Invalid activation code.");
    }

    User user = userRepository.findById(emailChange.getUser().getId())
        .orElseThrow(() -> new UserException("Error: Invalid activation code."));

    if (user == null) {
      throw new UserException("Error: Invalid activation code.");
    }

    emailChangeRequestRepository.deleteById(emailChange.getId());

    user.setEmail(emailChange.getNewEmail());
    user.setUpdatedAt(Instant.now());
    userRepository.save(user);

    return "Email activated successfully!";
  }

  /**
   * It takes a file, uploads it to firebase storage, and then saves the url to
   * the database
   * 
   * @param file The file to be uploaded.
   * @return A UserResponseDTO object
   */
  @Transactional
  public UserResponseDTO updateUserImage(MultipartFile file) throws IOException {

    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new UserException("Could not find user id= " + userDetails.getId()));
    String urlfile = firebaseFileService.saveFile(file);

    if (!user.getImageUrl()
        .equals("https://cdn.pixabay.com/photo/2016/04/01/11/25/avatar-1300331_960_720.png")) {
      firebaseFileService.deletFile(user.getImageUrl());
    }

    user.setImageUrl(
        "https://firebasestorage.googleapis.com/v0/b/yamara-db-image.appspot.com/o/" + urlfile + "?alt=media");
    user.setUpdatedAt(Instant.now());
    user = userRepository.save(user);

    return new UserResponseDTO(user);
  }

  /**
   * It deletes a user by setting the isActive flag to false
   * 
   * @param id The id of the user to be deleted
   */

  @Transactional
  public void deleteLogicalUserAdmin(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserException("Could not find user id= " + id));
    user.setUpdatedAt(Instant.now());
    user.setIsActive(false);
    userRepository.save(user);
  }

  /**
   * It deletes the user from the database by setting the isActive field to false
   */
  @Transactional
  public void deleteLogicalLoggedUser() {
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new UserException("Could not find user id= " + userDetails.getId()));
    user.setUpdatedAt(Instant.now());
    user.setIsActive(false);
    userRepository.save(user);
  }
}

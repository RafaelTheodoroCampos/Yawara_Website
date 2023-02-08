package br.com.yamarasolution.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.yamarasolution.DTO.users.EmailChangeRequestDTO;
import br.com.yamarasolution.DTO.users.UserRequestAdminDTO;
import br.com.yamarasolution.DTO.users.UserRequestDTO;
import br.com.yamarasolution.DTO.users.UserResponseDTO;
import br.com.yamarasolution.exception.ApiError;
import br.com.yamarasolution.exception.UserException;
import br.com.yamarasolution.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Usuarios")
public class UserController {

  @Autowired
  private UserService userService;

  /**
   * It returns a ResponseEntity with the logged user
   * 
   * @return A ResponseEntity with a body of type Object.
   */
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @GetMapping("/logged")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Get Logged User", description = "Get Logged User", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get User!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findLoggedUser() {
    try {
      return ResponseEntity.ok(userService.findLoggedUser());
    } catch (UserException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Get all users
   * 
   * @return A list of UserResponseDTO objects.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  @SecurityRequirement(name = "token")
  @Operation(summary = "Get all Users", description = "Get all Users, Admin Only", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get all!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<List<UserResponseDTO>> findAll() {
    return ResponseEntity.ok(userService.findAllUsers());
  }

  /**
   * Get User by Id, Admin Only
   * 
   * @param id The id of the user to be retrieved
   * @return A ResponseEntity with a body of an ApiError.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Get User by Id", description = "Get User by Id, Admin Only", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get by id!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findById(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(userService.findUserById(id));
    } catch (UserException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Get User by Username, Admin Only
   * 
   * @param username String
   * @return A ResponseEntity with a body of an ApiError.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/username/{username}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Get User by Username", description = "Get User by Username, Admin Only", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get by Username!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findByUsername(@PathVariable String username) {
    try {
      return ResponseEntity.ok(userService.findUserByUsername(username));
    } catch (UserException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Get User by Username, Admin Only
   * 
   * @param email String
   * @return A ResponseEntity with a body of an ApiError.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/email/{email}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Get User by Username", description = "Get User by Username, Admin Only", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get by Username!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findByEmail(@PathVariable String email) {
    try {
      return ResponseEntity.ok(userService.findUserByEmail(email));
    } catch (UserException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Update user, Admin Only
   * 
   * @param id          UUID
   * @param userRequest UserRequestAdminDTO
   * @return A ResponseEntity with a body of an ApiError.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/update/{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Update user", description = "Update admin, Admin Only", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully updated user!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> updateUserAdmin(@Valid @PathVariable UUID id,
      @RequestBody UserRequestAdminDTO userRequest) {
    try {
      UserResponseDTO response = userService.updateUserAdmin(id, userRequest);
      URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
          .buildAndExpand(response.getId())
          .toUri();
      return ResponseEntity.created(uri).body(response);
    } catch (UserException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Update user logged
   * 
   * @param userRequest UserRequestDTO
   * @return A ResponseEntity object.
   */
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @PutMapping("/update")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Update user logged", description = "Update user", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully updated user!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> updateUser(@Valid @RequestBody UserRequestDTO userRequest) {
    try {
      UserResponseDTO response = userService.updateUser(userRequest);
      URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
          .buildAndExpand(response.getId())
          .toUri();
      return ResponseEntity.created(uri).body(response);
    } catch (UserException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Update email logged user
   * 
   * @param emailChangeRequest
   * @return ResponseEntity.ok("Email send");
   */
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @PutMapping("/update/email")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Update email logged user", description = "Update email", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully updated user!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<?> updateEmail(@Valid @RequestBody EmailChangeRequestDTO emailChangeRequest) {
    try {
      userService.updateUserEmail(emailChangeRequest);
      return ResponseEntity.ok("Email send");
    } catch (UserException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * It takes a code as a parameter, and if the code is valid, it changes the
   * user's email
   * 
   * @param code The code that was sent to the user's email address
   * @return A ResponseEntity object.
   */
  @PutMapping("/update/email/confirm")
  @Operation(summary = "Confirm email", description = "Update email", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully updated email user!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> updateEmailConfirm(@Valid @RequestParam String code) {
    try {
      userService.confirmEmailChangeRequest(code);
      return ResponseEntity.ok("Email confirmation successfully");
    } catch (UserException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Update image logged user
   * 
   * @param file The file to upload.
   * @return A ResponseEntity object.
   */
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @PutMapping(value = "/update/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @SecurityRequirement(name = "token")
  @Operation(summary = "Update image logged user", description = "Update image", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully updated user!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> updateUserImage(@RequestParam(name = "file") MultipartFile file) {
    try {
      long maxFileSize = 5000000; // 5 MB
      System.out.println(file.getSize() + " dasdasdasdasd");
      if (file.getSize() > maxFileSize) {
        return ResponseEntity.unprocessableEntity()
            .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "File size exceeds the maximum allowed.",
                "Maximum upload size exceeded"));
      }
      UserResponseDTO response = userService.updateUserImage(file);
      return ResponseEntity.ok(response);
    } catch (IOException | MaxUploadSizeExceededException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Delete logical user admin
   * 
   * @param id UUID
   * @return ResponseEntity.unprocessableEntity()
   */
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/deleteAdmin")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Delete logical user", description = "Delete user admin", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully delete user!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> deleteAdmin(@PathVariable UUID id) {
    try {
      userService.deleteLogicalUserAdmin(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (UserException | DataIntegrityViolationException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Delete logical user
   * 
   * @return ResponseEntity.unprocessableEntity()
   */
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @DeleteMapping("/delete")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Delete logical user", description = "Delete user", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully delete user!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> delete() {
    try {
      userService.deleteLogicalLoggedUser();
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (UserException | DataIntegrityViolationException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

}

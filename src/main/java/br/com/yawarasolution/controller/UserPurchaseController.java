package br.com.yawarasolution.controller;

import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.yawarasolution.DTO.userpurchase.UserPurchaseRequestDTO;
import br.com.yawarasolution.DTO.userpurchase.UserPurchaseResponseDTO;
import br.com.yawarasolution.enums.PurchaseStatus;
import br.com.yawarasolution.exception.ApiError;
import br.com.yawarasolution.exception.UserPurchaseException;
import br.com.yawarasolution.service.UserPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "orders", description = "Pedidos do usuario")
public class UserPurchaseController {

  @Autowired
  private UserPurchaseService userPurchaseService;

  /**
   * Get all Products
   * 
   * @return A list of UserPurchaseResponseDTO
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  @SecurityRequirement(name = "token")
  @Operation(summary = "Get all Orders", description = "Get all Orders, only admin", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get all!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPurchaseResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<List<UserPurchaseResponseDTO>> findAll() {
    return ResponseEntity.ok(userPurchaseService.findAllUserPurchase());
  }

  /**
   * Get Orders by id
   * 
   * @param id The id of the user purchase
   * @return A ResponseEntity with a body of type Object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("admin/{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Get Orders by id", description = "Get Orders by id, only admin", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPurchaseResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findById(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(userPurchaseService.findUserPurchasById(id));
    } catch (UserPurchaseException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Get Orders by id loggedUser
   * 
   * @param id The id of the user purchase
   * @return The response is a UserPurchaseResponseDTO object.
   */
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @GetMapping("{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Get Orders by id loggedUser", description = "Get Orders by id", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPurchaseResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findByIdLogged(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(userPurchaseService.findUserPurchasLoogedUserById(id));
    } catch (UserPurchaseException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * This function returns a list of purchases made by the user, and the list can
   * be filtered by status
   * 
   * @param status The purchase status 1 - PENDING 2 - APPROVED 3 - REJECTED
   * @param p      Pageable
   * @return A list of UserPurchaseResponseDTO
   */
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @GetMapping("/search")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Search Orders", description = "Search Orders", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPurchaseResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  }, parameters = {
      @Parameter(name = "status", description = "The purchase status 1 - PENDING 2 - APPROVED 3 - REJECTED", example = "1"),
      @Parameter(name = "page", description = "The page number", example = "0"),
      @Parameter(name = "size", description = "The page size", example = "10")
  })
  public ResponseEntity<Object> searchOrders(
      @RequestParam(required = false) String status,
      @PageableDefault(page = 0, size = 10) @Parameter(hidden = true) Pageable p) {
    try {
      PurchaseStatus purchaseStatus;
      if (status == null) {
        return ResponseEntity.ok(userPurchaseService.searchUserPurchases(p));
      } else {
        try {
          // Tenta converter a string em enum
          purchaseStatus = PurchaseStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
          // Se não conseguir, tenta converter o número em enum
          try {
            purchaseStatus = PurchaseStatus.fromCodigo(status);
          } catch (NullPointerException ex) {
            // Se não conseguir, retorna erro
            return ResponseEntity.badRequest().body("Invalid status");
          }
        }
        return ResponseEntity.ok(userPurchaseService.searchUserPurchasesByStatus(purchaseStatus, p));
      }
    } catch (UserPurchaseException | DateTimeParseException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * This function is used to create a new order
   * 
   * @param orderRquest This is the object that will be passed to the method.
   * @return A ResponseEntity object.
   */
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @PostMapping("/register")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Create new Order", description = "Create new Order", responses = {
      @ApiResponse(responseCode = "201", description = "Successfully Register!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPurchaseResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> insert(@Valid @RequestBody UserPurchaseRequestDTO orderRquest) {
    try {
      UserPurchaseResponseDTO response = userPurchaseService.createOrder(orderRquest);
      URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
          .buildAndExpand(response.getId())
          .toUri();
      return ResponseEntity.created(uri).body(response);
    } catch (UserPurchaseException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }
}

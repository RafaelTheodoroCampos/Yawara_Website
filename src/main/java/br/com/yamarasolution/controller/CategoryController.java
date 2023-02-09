package br.com.yamarasolution.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.yamarasolution.DTO.category.CategoryRequestDTO;
import br.com.yamarasolution.DTO.category.CategoryResponseDTO;
import br.com.yamarasolution.exception.ApiError;
import br.com.yamarasolution.exception.CategoryException;
import br.com.yamarasolution.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/category")
@Tag(name = "Category", description = "Categoria dos produtos")
public class CategoryController {

  @Autowired
  private CategoryService categoryService;

  /**
   * Get all Categories
   * 
   * @return A list of CategoryResponseDTO objects.
   */
  @GetMapping
  @Operation(summary = "Get all Categories", description = "Get all Categories", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get all! Only active products will appear", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<List<CategoryResponseDTO>> findAll() {
    return ResponseEntity.ok(categoryService.findAllCategories());
  }

  /**
   * This function is used to get a category by id
   * 
   * @param id The id of the category to be retrieved.
   * @return A ResponseEntity object.
   */
  @GetMapping("{id}")
  @Operation(summary = "Get Categories by Id", description = "Get Categories by Id", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get by id! Only active products will appear", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findById(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(categoryService.findCategoryById(id));
    } catch (CategoryException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Get Categories by Name
   * 
   * @param name The name of the parameter.
   * @return A ResponseEntity object.
   */
  @GetMapping("/name/{name}")
  @Operation(summary = "Get Categories by Name", description = "Get Categories by Name", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get by Name! Only active products will appear", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findByName(@PathVariable String name) {
    try {
      return ResponseEntity.ok(categoryService.findCategoryByName(name));
    } catch (CategoryException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * This function is used to create a new category
   * 
   * @param categoryRequest This is the request body that is sent to the server.
   * @return The response is a CategoryResponseDTO object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/register")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Create new Category", description = "Create new Category", responses = {
      @ApiResponse(responseCode = "201", description = "Successfully Register!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> insert(@Valid @RequestBody CategoryRequestDTO categoryRequest) {
    try {
      CategoryResponseDTO response = categoryService.insertCategory(categoryRequest);
      URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
          .buildAndExpand(response.getId())
          .toUri();
      return ResponseEntity.created(uri).body(response);
    } catch (CategoryException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * It updates a category with the given id
   * 
   * @param id              The id of the category to be updated
   * @param categoryRequest This is the request body that is sent to the server.
   * @return A ResponseEntity object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/update/{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Update Category", description = "Update Category", responses = {
      @ApiResponse(responseCode = "201", description = "Successfully Updated!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> update(@Valid @PathVariable UUID id,
      @RequestBody CategoryRequestDTO categoryRequest) {
    try {
      CategoryResponseDTO response = categoryService.updateCategory(id, categoryRequest);
      URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
          .buildAndExpand(response.getId())
          .toUri();
      return ResponseEntity.created(uri).body(response);
    } catch (CategoryException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * It deletes a category by id
   * 
   * @param id The id of the category to be deleted
   * @return A ResponseEntity object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/delete/{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Delete Category", description = "Delete Category", responses = {
      @ApiResponse(responseCode = "204", description = "Successfully Deleted!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> delete(@PathVariable UUID id) {
    try {
      categoryService.deleteCategory(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (CategoryException | DataIntegrityViolationException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }
}

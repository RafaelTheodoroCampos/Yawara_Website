package br.com.yamarasolution.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.yamarasolution.DTO.product.ProducRequestDTO;
import br.com.yamarasolution.DTO.product.ProductResponseDTO;
import br.com.yamarasolution.exception.ApiError;
import br.com.yamarasolution.exception.ProductException;
import br.com.yamarasolution.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Products")
public class ProductController {

  @Autowired
  private ProductService productService;

  /**
   * Get all Products
   * 
   * @return A list of ProductResponseDTO objects.
   */
  @GetMapping
  @Operation(summary = "Get all Products", description = "Get all Products", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get all!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<List<ProductResponseDTO>> findAll() {
    return ResponseEntity.ok(productService.findAllProducts());
  }

  /**
   * It returns a list of products that match the search criteria
   * 
   * @param name     String
   * @param isActive true or false
   * @param p        Pageable
   * @return A Page of ProductResponseDTO
   */
  @GetMapping("/search")
  @Operation(summary = "Search products peable", description = "Get all Products peable", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get all!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  }, parameters = {
      @Parameter(name = "name", description = "The name of the product to search for", example = "Rtx-3090"),
      @Parameter(name = "isActive", description = "Indicates whether the product is active or not", example = "true"),
      @Parameter(name = "page", description = "The page number", example = "0"),
      @Parameter(name = "size", description = "The page size", example = "10"),
  })
  public ResponseEntity<Page<ProductResponseDTO>> searchProducts(@RequestParam(required = false) String name,
      @RequestParam(required = true) Boolean isActive,
      @PageableDefault(page = 0, size = 10) @Parameter(hidden = true)  Pageable p) {
    if (name == null) {
      return ResponseEntity.ok(productService.findAllProductsPageable(isActive, p));
    }
    return ResponseEntity.ok(productService.searchProducts(name, isActive, p));
  }

  /**
   * This function returns a product by id
   * 
   * @param id The id of the product to be retrieved.
   * @return ResponseEntity&lt;Object&gt;
   */
  @GetMapping("{id}")
  @Operation(summary = "Get Product by Id", description = "Get Product by Id", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get by id!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> findById(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(productService.findProductsById(id));
    } catch (ProductException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * It creates a new product, and returns the created product
   * 
   * @param file    The file to upload
   * @param produto is a DTO that contains the product information
   * @return The response is a 201 status code with the body of the response being
   *         the ProductResponseDTO
   *         object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(value = "/register", consumes = "multipart/form-data")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Create new Product", description = "Create new Product", responses = {
      @ApiResponse(responseCode = "201", description = "Successfully Register!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> create(@RequestParam("file") MultipartFile file,
      @Valid @RequestPart(value = "produto") @Parameter(schema = @Schema(type = "string", format = "binary")) ProducRequestDTO produto)
      throws IOException {
    try {
      long maxFileSize = 5000000; // 5 MB
      if (file.getSize() > maxFileSize) {
        return ResponseEntity.unprocessableEntity()
            .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "File size exceeds the maximum allowed.",
                "Maximum upload size exceeded"));
      }
      ProductResponseDTO response = productService.createProduct(produto, file);
      URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
          .buildAndExpand(response.getId())
          .toUri();
      return ResponseEntity.created(uri).body(response);
    } catch (ProductException | IOException | MaxUploadSizeExceededException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

}

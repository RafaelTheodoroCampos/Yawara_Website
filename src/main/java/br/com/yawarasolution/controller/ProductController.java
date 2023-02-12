package br.com.yawarasolution.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.yawarasolution.DTO.product.ProducRequestDTO;
import br.com.yawarasolution.DTO.product.ProductResponseDTO;
import br.com.yawarasolution.exception.ApiError;
import br.com.yawarasolution.exception.ProductException;
import br.com.yawarasolution.service.ProductService;
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
   * It searches for products by name, category and isActive, and returns a
   * pageable response
   * 
   * @param name         The name of the product to search for
   * @param categoryName Category to which the product belongs
   * @param isActive     Indicates whether the product is active or not
   * @param p            Pageable
   * @return A list of products
   */
  @GetMapping("/search")
  @Operation(summary = "Search products peable", description = "Get all Products peable", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully get all!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  }, parameters = {
      @Parameter(name = "name", description = "The name of the product to search for", example = "Calabresa"),
      @Parameter(name = "categoryName", description = "Category to which the product belongs", example = "Pizzas"),
      @Parameter(name = "isActive", description = "Indicates whether the product is active or not", example = "true"),
      @Parameter(name = "page", description = "The page number", example = "0"),
      @Parameter(name = "size", description = "The page size", example = "10"),
  })
  public ResponseEntity<Object> searchProductsAndCategory(@RequestParam(required = false) String name,
      @RequestParam(required = false) String categoryName,
      @RequestParam(required = true) Boolean isActive,
      @PageableDefault(page = 0, size = 10) @Parameter(hidden = true) Pageable p) {
    try {
      if (name == null && categoryName == null) {
        return ResponseEntity.ok(productService.findAllProductsPageable(isActive, p));
      }
      if (name == null) {
        return ResponseEntity.ok(productService.findAllProductsByCategyPageable(categoryName, isActive, p));
      }
      if (categoryName == null) {
        return ResponseEntity.ok(productService.searchProducts(name, isActive, p));
      }
      return ResponseEntity.ok(productService.searchProductsByCategories(name, categoryName, isActive, p));
    } catch (ProductException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * This function returns a product by id
   * 
   * @param id The id of the product to be retrieved.
   * @return ResponseEntity&lt;Object&gt;
   */
  @GetMapping("{id}")
  @Operation(summary = "Get Active Product by Id", description = "Get Active Product by Id", responses = {
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
  public ResponseEntity<Object> create(
      @RequestParam("file") @Parameter(name = "file", description = "The product photo") MultipartFile file,
      @Valid @RequestPart(value = "produto") @Parameter(name = "produto", description = "product json", schema = @Schema(type = "string", format = "binary")) ProducRequestDTO produto)
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

  /**
   * Update a product by id
   * 
   * @param id      The id of the product to be updated
   * @param produto is the object that will be updated
   * @return The response is a ProductResponseDTO object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/update/{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Update Product", description = "Update Product", responses = {
      @ApiResponse(responseCode = "201", description = "Successfully Updated!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> update(@PathVariable UUID id, @Valid @RequestBody ProducRequestDTO produto) {
    try {
      ProductResponseDTO response = productService.updateProduct(id, produto);
      URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
          .buildAndExpand(response.getId())
          .toUri();
      return ResponseEntity.created(uri).body(response);
    } catch (ProductException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * It updates the product image
   * 
   * @param id   The id of the product to be updated
   * @param file The file to upload.
   * @return A ResponseEntity object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/update-image/{id}", consumes = "multipart/form-data")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Update Product image", description = "Update Product image", responses = {
      @ApiResponse(responseCode = "201", description = "Successfully Updated!", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class))),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> updateProductImage(@PathVariable UUID id,
      @RequestParam(name = "file") MultipartFile file) {
    try {
      long maxFileSize = 5000000; // 5 MB
      if (file.getSize() > maxFileSize) {
        return ResponseEntity.unprocessableEntity()
            .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "File size exceeds the maximum allowed.",
                "Maximum upload size exceeded"));
      }
      ProductResponseDTO response = productService.updateProductImage(id, file);
      return ResponseEntity.ok(response);
    } catch (IOException | MaxUploadSizeExceededException | ProductException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Delete product
   * 
   * @param id The id of the product to be deleted
   * @return The response is a 204 No Content.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/delete-logical/{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Delete logical product", description = "Delete product", responses = {
      @ApiResponse(responseCode = "204", description = "Successfully Deleted!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> deleteLogical(@PathVariable UUID id) {
    try {
      productService.deleteLogicalProduct(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (ProductException | DataIntegrityViolationException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Delete a product by id
   * 
   * @param id The id of the product to be deleted
   * @return The response is a ResponseEntity object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/delete/{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Delete product", description = "Delete product", responses = {
      @ApiResponse(responseCode = "204", description = "Successfully Deleted!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> delete(@PathVariable UUID id) {
    try {
      productService.deleteProduct(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (ProductException | DataIntegrityViolationException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }

  /**
   * Reactive a product by id
   * 
   * @param id The id of the product to be reative
   * @return The response is a ResponseEntity object.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/reactive-products/{id}")
  @SecurityRequirement(name = "token")
  @Operation(summary = "Reactive product", description = "Reactive product", responses = {
      @ApiResponse(responseCode = "204", description = "Successfully Reactivated!"),
      @ApiResponse(responseCode = "400", ref = "BadRequest"),
      @ApiResponse(responseCode = "401", ref = "badcredentials"),
      @ApiResponse(responseCode = "403", ref = "forbidden"),
      @ApiResponse(responseCode = "422", ref = "unprocessableEntity"),
      @ApiResponse(responseCode = "500", ref = "internalServerError")
  })
  public ResponseEntity<Object> reactive(@PathVariable UUID id) {
    try {
      productService.reactiveProduct(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (ProductException e) {
      return ResponseEntity.unprocessableEntity()
          .body(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getLocalizedMessage()));
    }
  }
}

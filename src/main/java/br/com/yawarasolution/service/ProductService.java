package br.com.yawarasolution.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.yawarasolution.DTO.product.ProducRequestDTO;
import br.com.yawarasolution.DTO.product.ProductResponseDTO;
import br.com.yawarasolution.exception.ProductException;
import br.com.yawarasolution.model.Category;
import br.com.yawarasolution.model.Product;
import br.com.yawarasolution.model.User;
import br.com.yawarasolution.repository.CategoryRepository;
import br.com.yawarasolution.repository.ProductRepository;
import br.com.yawarasolution.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class ProductService {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private FirebaseFileService firebaseFileService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  /**
   * It takes all the products from the database, converts them to a
   * ProductResponseDTO object, and
   * returns a list of ProductResponseDTO objects
   * 
   * @return A list of ProductResponseDTO objects.
   */
  public List<ProductResponseDTO> findAllProducts() {
    return productRepository.findAll().stream()
        .map(ProductResponseDTO::new).collect(Collectors.toList());
  }

  /**
   * It takes a UUID id, finds the product in the database, maps it to a
   * ProductResponseDTO, and returns
   * it
   * 
   * @param id The id of the product to be found
   * @return A ProductResponseDTO object.
   */
  public ProductResponseDTO findProductsById(UUID id) {
    return productRepository.findById(id).filter(p -> p.getIsActive())
        .map(ProductResponseDTO::new)
        .orElseThrow(() -> new ProductException("Could not find product, id= " + id));
  }

  /**
   * Find all products that are active, and return a page of them, mapped to
   * ProductResponseDTO objects.
   * 
   * @param isActive Boolean
   * @param pageable The pageable object is used to specify the page number, page
   *                 size, and sort order.
   * @return A Page of ProductResponseDTOs
   */
  public Page<ProductResponseDTO> findAllProductsPageable(Boolean isActive, Pageable pageable) {
    Page<Product> products = productRepository.findByIsActive(isActive, pageable);
    return products.map(ProductResponseDTO::new);
  }

  /**
   * It returns a page of products by category name and isActive.
   * 
   * @param categoryName The name of the category you want to search for.
   * @param isActive     true
   * @param pageable     This is the pageable object that contains the page
   *                     number, page size, and sort
   *                     order.
   * @return A Page of ProductResponseDTOs
   */
  public Page<ProductResponseDTO> findAllProductsByCategyPageable(String categoryName, Boolean isActive,
      Pageable pageable) {
    Page<Product> products = productRepository.findByCategory_NameIgnoreCaseAndIsActive(categoryName, isActive,
        pageable);
    return products.map(ProductResponseDTO::new);
  }

  /**
   * It searches for products by name, category name and isActive flag
   * 
   * @param name         The name of the product
   * @param categoryName The name of the category to search for.
   * @param isActive     boolean
   * @param pageable     This is the pageable object that contains the page number
   *                     and page size.
   * @return A Page of ProductResponseDTOs
   */
  public Page<ProductResponseDTO> searchProductsByCategories(String name, String categoryName, boolean isActive,
      Pageable pageable) {
    if (pageable == null || pageable.getPageNumber() < 0 || pageable.getPageSize() < 1) {
      throw new ProductException("Invalid page request");
    }

    Page<Product> products = productRepository.findByNameContainingIgnoreCaseAndCategory_NameIgnoreCaseAndIsActive(name,
        categoryName, isActive, pageable);

    if (products == null || products.isEmpty()) {
      throw new ProductException(
          "No products found for name: " + name + " and isActive: " + isActive + " and category: " + categoryName);
    }

    return products.map(ProductResponseDTO::new);
  }

  /**
   * It takes a name, isActive and pageable as input and returns a page of
   * ProductResponseDTO
   * 
   * @param name     The name of the product.
   * @param isActive true
   * @param pageable This is the pageable object that contains the page number,
   *                 page size, and sort
   *                 order.
   * @return A Page of ProductResponseDTOs
   */
  public Page<ProductResponseDTO> searchProducts(String name, Boolean isActive, Pageable pageable) {

    if (pageable == null || pageable.getPageNumber() < 0 || pageable.getPageSize() < 1) {
      throw new ProductException("Invalid page request");
    }

    Page<Product> products = productRepository.findByNameContainingIgnoreCaseAndIsActive(name, isActive, pageable);

    if (products == null || products.isEmpty()) {
      throw new ProductException("No products found for name: " + name + " and isActive: " + isActive);
    }

    return products.map(ProductResponseDTO::new);
  }

  /**
   * It creates a product and uploads the image to firebase
   * 
   * @param productRequest This is the request body that is sent to the API.
   * @param file           MultipartFile
   * @return The ProductResponseDTO is being returned.
   */
  @Transactional
  public ProductResponseDTO createProduct(ProducRequestDTO productRequest, MultipartFile file) throws IOException {

    // This is checking if the product name already exists in the database.
    String name = productRequest.getName();
    if (productRepository.existsByNameIgnoreCase(name)) {
      throw new ProductException("Name already exists for Product, name= " + name);
    }

    // This is getting the user from the security context.
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new ProductException("Could not find user id= " + userDetails.getId()));

    // Finding the category by id and throwing an exception if it is not found.
    Category category = categoryRepository.findById(productRequest.getCategory().getId())
        .orElseThrow(
            () -> new ProductException("Could not find category, id= " + productRequest.getCategory().getId()));

    // Upload Image to Firebase
    String urlfile = firebaseFileService.saveFile(file);

    Product product = new Product();
    product.setCategory(category);
    product.setCreatedAt(Instant.now());
    product.setCreatedBy(user);
    product.setDescription(productRequest.getDescription());
    product.setImageUrl(
        "https://firebasestorage.googleapis.com/v0/b/yamara-db-image.appspot.com/o/" + urlfile + "?alt=media");
    product.setIsActive(true);
    product.setName(name);
    product.setPrice(productRequest.getPrice());
    product.setRating(5);
    product.setStock(productRequest.getStock());
    product.setUpdatedAt(null);
    product = productRepository.save(product);

    return new ProductResponseDTO(product);
  }

  /**
   * It updates a product in the database
   * 
   * @param id             The id of the product to be updated.
   * @param productRequest This is the request body that is sent to the API.
   * @return A ProductResponseDTO object.
   */
  @Transactional
  public ProductResponseDTO updateProduct(UUID id, ProducRequestDTO productRequest) {

    // Finding the product by id and throwing an exception if it is not found.
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductException("Could not find product, id= " + id));

    // This is checking if the product name already exists in the database.
    String name = productRequest.getName();
    if (!product.getName().equalsIgnoreCase(name) && productRepository.existsByNameIgnoreCase(name)) {
      throw new ProductException("Name already exists for category name= " + name);
    }

    // Finding the category by id and throwing an exception if it is not found.
    Category category = categoryRepository.findById(productRequest.getCategory().getId())
        .orElseThrow(
            () -> new ProductException("Could not find category, id= " + productRequest.getCategory().getId()));

    product.setCategory(category);
    product.setCreatedAt(product.getCreatedAt());
    product.setCreatedBy(product.getCreatedBy());
    product.setDescription(productRequest.getDescription());
    product.setImageUrl(product.getImageUrl());
    product.setIsActive(true);
    product.setName(name);
    product.setPrice(productRequest.getPrice());
    product.setRating(product.getRating());
    product.setStock(productRequest.getStock());
    product.setUpdatedAt(Instant.now());
    product = productRepository.save(product);

    return new ProductResponseDTO(product);

  }

  /**
   * It finds a product by id, uploads a new image to firebase, deletes the old
   * image from firebase, and
   * updates the product with the new image url
   * 
   * @param id   The id of the product to be updated.
   * @param file The file to be uploaded.
   * @return The ProductResponseDTO is being returned.
   */
  @Transactional
  public ProductResponseDTO updateProductImage(UUID id, MultipartFile file) throws IOException {

    // Finding the product by id and throwing an exception if it is not found.
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductException("Could not find product, id= " + id));

    // Uploading the image to firebase.
    String urlfile = firebaseFileService.saveFile(file);
    // Deleting the image from firebase.
    firebaseFileService.deletFile(product.getImageUrl());

    product.setImageUrl(
        "https://firebasestorage.googleapis.com/v0/b/yamara-db-image.appspot.com/o/" + urlfile + "?alt=media");
    product.setUpdatedAt(Instant.now());
    product = productRepository.save(product);
    return new ProductResponseDTO(product);
  }

  /**
   * It finds the product by id, sets the updatedAt and isActive fields, and saves
   * the product
   * 
   * @param id The id of the product to be deleted.
   */
  @Transactional
  public void deleteLogicalProduct(UUID id) {
    // Finding the product by id and throwing an exception if it is not found.
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductException("Could not find product, id= " + id));
    product.setUpdatedAt(Instant.now());
    product.setIsActive(false);
    productRepository.save(product);
  }

  /**
   * It deletes a product if it has no purchases
   * 
   * @param id The id of the product to be deleted.
   */
  @Transactional
  public void deleteProduct(UUID id) {
    // Finding the product by id and throwing an exception if it is not found.
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductException("Could not find product, id= " + id));

    if (hasPurchases(product)) {
      throw new ProductException("Product has purchases, cannot be deleted");
    }

    productRepository.deleteById(id);
  }

  /**
   * If the product has no purchases, then it is not a valid product
   * 
   * @param product The product to check for purchases
   * @return A boolean value.
   */
  private boolean hasPurchases(Product product) {
    return product.getPurchases().size() > 0;
  }

  /**
   * It finds the product by id, sets the updatedAt and isActive fields, and saves
   * the product
   * 
   * @param id The id of the product to be reactivated.
   */
  @Transactional
  public void reactiveProduct(UUID id) {
    // Finding the product by id and throwing an exception if it is not found.
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductException("Could not find product, id= " + id));
    product.setUpdatedAt(Instant.now());
    product.setIsActive(true);
    productRepository.save(product);
  }
}

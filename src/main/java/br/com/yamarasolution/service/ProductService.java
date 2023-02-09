package br.com.yamarasolution.service;

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

import br.com.yamarasolution.DTO.product.ProducRequestDTO;
import br.com.yamarasolution.DTO.product.ProductResponseDTO;
import br.com.yamarasolution.exception.ProductException;
import br.com.yamarasolution.model.Category;
import br.com.yamarasolution.model.Product;
import br.com.yamarasolution.model.User;
import br.com.yamarasolution.repository.CategoryRepository;
import br.com.yamarasolution.repository.ProductRepository;
import br.com.yamarasolution.repository.UserRepository;
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
    return productRepository.findById(id)
        .map(ProductResponseDTO::new)
        .orElseThrow(() -> new ProductException("Could not find product, id= " + id));
  }

  /**
   * Find all products that are active, and return a page of them, mapped to ProductResponseDTO objects.
   * 
   * @param isActive Boolean
   * @param pageable The pageable object is used to specify the page number, page size, and sort order.
   * @return A Page of ProductResponseDTOs
   */
  public Page<ProductResponseDTO> findAllProductsPageable(Boolean isActive, Pageable pageable) {
    Page<Product> products = productRepository.findByIsActive(isActive, pageable);
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
    Page<Product> products = productRepository.findByNameContainingIgnoreCaseAndIsActive(name, isActive, pageable);
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

}

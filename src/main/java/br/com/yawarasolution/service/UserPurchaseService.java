package br.com.yawarasolution.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.com.yawarasolution.DTO.userpurchase.PurchaseRequestUserPurchaseDTO;
import br.com.yawarasolution.DTO.userpurchase.UserPurchaseRequestDTO;
import br.com.yawarasolution.DTO.userpurchase.UserPurchaseResponseDTO;
import br.com.yawarasolution.config.MailConfig;
import br.com.yawarasolution.enums.PurchaseStatus;
import br.com.yawarasolution.exception.UserPurchaseException;
import br.com.yawarasolution.model.Product;
import br.com.yawarasolution.model.Purchase;
import br.com.yawarasolution.model.User;
import br.com.yawarasolution.model.UserPurchase;
import br.com.yawarasolution.repository.ProductRepository;
import br.com.yawarasolution.repository.PurchaseRepository;
import br.com.yawarasolution.repository.UserPurchaseRepository;
import br.com.yawarasolution.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserPurchaseService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PurchaseRepository purchaseRepository;

  @Autowired
  private UserPurchaseRepository userPurchaseRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private MailConfig mailConfig;

  /**
   * It takes all the user purchases from the database, converts them to
   * UserPurchaseResponseDTO objects,
   * and returns them as a list
   * 
   * @return A list of UserPurchaseResponseDTO objects.
   */
  public List<UserPurchaseResponseDTO> findAllUserPurchase() {
    return userPurchaseRepository.findAll().stream()
        .map(UserPurchaseResponseDTO::new).collect(Collectors.toList());
  }

  /**
   * It takes an id, finds the user purchase by that id, maps it to a
   * UserPurchaseResponseDTO, and
   * returns it
   * 
   * @param id UUID
   * @return UserPurchaseResponseDTO
   */
  public UserPurchaseResponseDTO findUserPurchasById(UUID id) {
    return userPurchaseRepository.findById(id)
        .map(UserPurchaseResponseDTO::new)
        .orElseThrow(() -> new UserPurchaseException("Could not find User purchase, id: " + id));
  }

  /**
   * This function is getting the user from the security context, then it's
   * getting the user purchase by
   * id, then it's filtering the user purchase by the user, then it's mapping the
   * user purchase to a
   * UserPurchaseResponseDTO, then it's returning the UserPurchaseResponseDTO
   * 
   * @param id The id of the user purchase.
   * @return A UserPurchaseResponseDTO object.
   */
  public UserPurchaseResponseDTO findUserPurchasLoogedUserById(UUID id) {
    // This is getting the user from the security context.
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new UserPurchaseException("Could not find user, id: " + userDetails.getId()));
    return userPurchaseRepository.findById(id).filter(us -> us.getUser() == user)
        .map(UserPurchaseResponseDTO::new)
        .orElseThrow(() -> new UserPurchaseException("Could not find User purchase, id: " + id));
  }

  /**
   * It searches for a user purchase by the Logged user and the purchase name
   * 
   * @param purchase This is the search query.
   * @param pageable This is the page number and the page size.
   * @return The searchUserPurchases method is returning a Page of
   *         UserPurchaseResponseDTO.
   */
  public Page<UserPurchaseResponseDTO> searchUserPurchases(Pageable pageable) {
    // This is getting the user from the security context.
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new UserPurchaseException("Could not find user, id: " + userDetails.getId()));

    // Checking if the page number is less than 0 or the page size is less than 1.
    // If it is, it throws an exception.
    if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1) {
      throw new UserPurchaseException("Invalid page request");
    }

    Page<UserPurchase> userPurchase = userPurchaseRepository.findByUser(user, pageable);

    return userPurchase.map(UserPurchaseResponseDTO::new);
  }

  /**
   * It gets the user from the security context, checks if the page number is less
   * than 0 or the page
   * size is less than 1, and if it is, it throws an exception. Then it gets the
   * user purchases by status
   * and user. If the user purchases are null or empty, it throws an exception. If
   * not, it returns the
   * user purchases
   * 
   * @param status   The status of the purchase.
   * @param pageable This is the pageable object that is passed in from the
   *                 controller.
   * @return A Page of UserPurchaseResponseDTOs.
   */
  public Page<UserPurchaseResponseDTO> searchUserPurchasesByStatus(PurchaseStatus status, Pageable pageable) {
    // This is getting the user from the security context.
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new UserPurchaseException("Could not find user, id: " + userDetails.getId()));

    // Checking if the page number is less than 0 or the page size is less than 1.
    // If it is, it throws an exception.
    if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1) {
      throw new UserPurchaseException("Invalid page request");
    }

    Page<UserPurchase> userPurchase = userPurchaseRepository.findBypurchaseStatusAndUser(status, user, pageable);

    if (userPurchase == null || userPurchase.isEmpty()) {
      throw new UserPurchaseException(
          "No UserPurchase found for User: " + user.getUsername() + " and status: " + status);
    }

    return userPurchase.map(UserPurchaseResponseDTO::new);
  }

  /**
   * This function is called when a user makes a purchase. It creates a new
   * UserPurchase object and saves
   * it to the database. It then creates a list of Purchase objects and saves them
   * to the database
   * 
   * @param purchaseRequest This is the request object that is passed in from the
   *                        controller.
   * @return A UserPurchaseResponseDTO object.
   */
  @Transactional
  public UserPurchaseResponseDTO createOrder(UserPurchaseRequestDTO purchaseRequest) {

    // This is getting the user from the security context.
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new UserPurchaseException("Could not find user, id: " + userDetails.getId()));

    UserPurchase userPurchase = new UserPurchase();
    userPurchase.setUser(user);
    userPurchase.setPurchaseStatus(PurchaseStatus.PENDING);
    userPurchase.setPurchaseDate(LocalDate.now());

    List<Purchase> purchases = new ArrayList<>();

    purchaseRequest.getPurchases().stream().forEach(p -> purchases.add(fromPurchaseProductRequestDTO(p)));

    BigDecimal totalPrice = purchases.stream()
        .map(p -> p.getUnitPrice().multiply(p.getQuantity()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    userPurchase.setTotalPrice(totalPrice);

    userPurchase = userPurchaseRepository.save(userPurchase);

    for (Purchase purchase : purchases) {
      purchase.setUserPurchase(userPurchase);
      purchaseRepository.save(purchase);
    }

    // Send email notification
    List<String> productNames = purchases.stream().map(p -> p.getProduct().getName()).collect(Collectors.toList());
    String productList = String.join(", ", productNames);
    String confirmationMessage = "Sua compra foi concluída com sucesso. Produtos comprados: " + productList
        + ". Status: " + userPurchase.getPurchaseStatus().getMensagem() + ". Valor total: " + userPurchase.getTotalPrice()
        + ".";

    mailConfig.sendEmail(user.getEmail(), "Thank you for your purchase", confirmationMessage);

    return new UserPurchaseResponseDTO(userPurchase, purchases);
  }

  /**
   * It updates the stock of a product by subtracting the quantity of the product
   * that was purchased
   * 
   * @param purchaseProduct This is the product that the user is trying to
   *                        purchase.
   * @param product         The product that the user is purchasing
   */
  @Transactional
  private void updateProductStock(PurchaseRequestUserPurchaseDTO purchaseProduct, Product product) {
    BigDecimal newStockQuantity = new BigDecimal(product.getStock()).subtract(purchaseProduct.getQuantity());
    if (newStockQuantity.compareTo(BigDecimal.ZERO) < 0) {
      throw new UserPurchaseException("Insufficient stock for product with id " + product.getId());
    }
    product.setStock(newStockQuantity.intValueExact());
    productRepository.save(product);
  }

  /**
   * It takes a PurchaseRequestUserPurchaseDTO object, finds the product in the
   * database, updates the
   * product stock, creates a new Purchase object, sets the product, unit price,
   * and quantity, and
   * returns the Purchase object
   * 
   * @param purchaseProduct PurchaseRequestUserPurchaseDTO
   * @return A Purchase object.
   */
  @Transactional
  private Purchase fromPurchaseProductRequestDTO(PurchaseRequestUserPurchaseDTO purchaseProduct) {
    Product product = productRepository.findById(purchaseProduct.getProduct().getId()).orElseThrow(
        () -> new UserPurchaseException("Product not found with id " + purchaseProduct.getProduct().getId()));
    updateProductStock(purchaseProduct, product);
    Purchase purchase = new Purchase();
    purchase.setProduct(product);
    purchase.setUnitPrice(new BigDecimal(product.getPrice()));
    purchase.setQuantity(purchaseProduct.getQuantity());
    return purchase;
  }

  /**
   * It updates the status of a purchase order
   * 
   * @param id            UUID
   * @param statusRequest CANCELED
   * @return The method returns a UserPurchaseResponseDTO object.
   */
  @Transactional
  public UserPurchaseResponseDTO updateOrderStatus(UUID id, PurchaseStatus statusRequest) {
    UserPurchase userPurchase = userPurchaseRepository.findById(id)
        .orElseThrow(() -> new UserPurchaseException("Could not find Order, id: " + id));

    if (userPurchase.getPurchaseStatus() == PurchaseStatus.CANCELED) {
      throw new UserPurchaseException("Purchase already canceled, impossible to change the status");
    }

    if (statusRequest == PurchaseStatus.CANCELED && userPurchase.getPurchaseStatus() != PurchaseStatus.CANCELED) {
      returnProductStock(userPurchase.getPurchases());
    }

    userPurchase.setPurchaseStatus(statusRequest);
    userPurchase = userPurchaseRepository.save(userPurchase);

    // Email notification
    String pattern = "dd/MM/yyyy HH:mm:ss";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    String mensagem = String.format("Status do pedido atualizado para '%s' às %s",
        statusRequest.getMensagem(), LocalDateTime.now().format(formatter));
    mailConfig.sendEmail(userPurchase.getUser().getEmail(), "Update on your purchase", mensagem);

    return new UserPurchaseResponseDTO(userPurchase);
  }

  /**
   * It returns the stock of the products purchased by the user
   * 
   * @param purchases List of purchases to be returned
   */
  @Transactional
  private void returnProductStock(List<Purchase> purchases) {
    for (Purchase purchase : purchases) {
      Product product = productRepository.findById(purchase.getProduct().getId())
          .orElseThrow(() -> new UserPurchaseException("Product not found, id: " + purchase.getProduct().getId()));
      product.setStock(new BigDecimal(product.getStock()).add(purchase.getQuantity()).intValueExact());
      productRepository.save(product);
    }
  }

}

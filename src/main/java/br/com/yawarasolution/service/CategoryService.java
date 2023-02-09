package br.com.yawarasolution.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.yawarasolution.DTO.category.CategoryRequestDTO;
import br.com.yawarasolution.DTO.category.CategoryResponseDTO;
import br.com.yawarasolution.exception.CategoryException;
import br.com.yawarasolution.model.Category;
import br.com.yawarasolution.repository.CategoryRepository;
import jakarta.transaction.Transactional;

@Service
public class CategoryService {

  @Autowired
  private CategoryRepository categoryRepository;

  /**
   * Find all categories, convert them to CategoryResponseDTOs, and return them as
   * a list.
   * 
   * @return A list of CategoryResponseDTO objects.
   */
  public List<CategoryResponseDTO> findAllCategories() {
    return categoryRepository.findAll().stream()
        .map(CategoryResponseDTO::new).collect(Collectors.toList());
  }

  /**
   * Find a category by id, if it exists, return a new CategoryResponseDTO,
   * otherwise throw a
   * CategoryException.
   * 
   * @param id The id of the category you want to find
   * @return A CategoryResponseDTO object
   */
  public CategoryResponseDTO findCategoryById(UUID id) {
    return categoryRepository.findById(id)
        .map(CategoryResponseDTO::new)
        .orElseThrow(() -> new CategoryException("Could not find category id= " + id));
  }

  /**
   * It takes a string, searches for a category with that name, and returns a list
   * of category response
   * DTOs
   * 
   * @param name The name of the category to search for.
   * @return A list of CategoryResponseDTO objects.
   */
  public List<CategoryResponseDTO> findCategoryByName(String name) {
    List<Category> categories = categoryRepository.findByNameEqualsIgnoreCase(name);
    if (categories.isEmpty()) {
      throw new CategoryException("Could not find category name= " + name);
    }
    return categories.stream().map(CategoryResponseDTO::new).collect(Collectors.toList());
  }

  /**
   * It takes a categoryRequest object, checks if the name already exists in the
   * database, if it doesn't,
   * it creates a new category object, sets the name and description, saves it to
   * the database, and
   * returns a categoryResponse object
   * 
   * @param categoryRequest
   * @return A CategoryResponseDTO object
   */
  @Transactional
  public CategoryResponseDTO insertCategory(CategoryRequestDTO categoryRequest) {

    String name = categoryRequest.getName();
    if (categoryRepository.existsByNameIgnoreCase(name)) {
      throw new CategoryException("Name already exists for category name= " + name);
    }

    Category category = new Category();
    category.setName(name);
    category.setDescription(categoryRequest.getDescription());
    category = categoryRepository.save(category);

    return new CategoryResponseDTO(category);

  }

  /**
   * It updates a category in the database
   * 
   * @param id              The id of the category to update
   * @param categoryRequest CategoryRequestDTO
   * @return A CategoryResponseDTO object
   */
  @Transactional
  public CategoryResponseDTO updateCategory(UUID id, CategoryRequestDTO categoryRequest) {

    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryException("Could not find category id= " + id));

    String name = categoryRequest.getName();
    if (!category.getName().equalsIgnoreCase(name) && categoryRepository.existsByNameIgnoreCase(name)) {
      throw new CategoryException("Name already exists for category name= " + name);
    }

    category.setName(name);
    category.setDescription(categoryRequest.getDescription());
    category = categoryRepository.save(category);

    return new CategoryResponseDTO(category);

  }

  /**
   * If the category exists, delete it
   * 
   * @param id The id of the category to delete
   */
  @Transactional
  public void deleteCategory(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryException("Could not find category id= " + id));

    if (hasProducts(category)) {
      throw new CategoryException("Category has products, cannot be deleted");
    }

    categoryRepository.deleteById(id);
  }

  /**
   * If the category has products, return true, otherwise return false.
   * 
   * @param category The category to check
   * @return A boolean value.
   */
  private boolean hasProducts(Category category) {
    return category.getProducts().size() > 0;
  }

}

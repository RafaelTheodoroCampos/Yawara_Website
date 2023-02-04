package br.com.yamarasolution.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.yamarasolution.DTO.category.CategoryRequestDTO;
import br.com.yamarasolution.DTO.category.CategoryResponseDTO;
import br.com.yamarasolution.exception.CategoryException;
import br.com.yamarasolution.model.Category;
import br.com.yamarasolution.repository.CategoryRepository;
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
        .orElseThrow(() -> new CategoryException("Could not find category " + id));
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
      throw new CategoryException("Could not find category " + name);
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
    if (categoryRepository.existsByName(name)) {
      throw new CategoryException("Name already exists for category " + name);
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
        .orElseThrow(() -> new CategoryException("Could not find category " + id));

    String name = categoryRequest.getName();
    if (!category.getName().equalsIgnoreCase(name) && categoryRepository.existsByName(name)) {
      throw new CategoryException("Name already exists for category " + name);
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
  public void deleteCategory(UUID id) {
    categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryException("Could not find category " + id));

    categoryRepository.deleteById(id);
  }

}
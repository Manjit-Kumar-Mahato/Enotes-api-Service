package com.prog.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.prog.dto.CategoryDto;
import com.prog.dto.CategoryResponse;
import com.prog.entity.Category;
import com.prog.exception.ExistDataException;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.CategoryRepository;
import com.prog.service.CategoryService;
import com.prog.util.Validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepo;
	
	private final ModelMapper mapper;
	
	private final Validation validation;

	@Override
	public Boolean saveCategory(CategoryDto categoryDto) {

		log.info("CategoryServiceImpl : saveCategory() : Processing category '{}'",
				categoryDto.getName());

		validation.categoryValidation(categoryDto);

		Boolean exist = categoryRepo.existsByName(categoryDto.getName().trim());

		if (exist) {
			log.warn("CategoryServiceImpl : saveCategory() : Category already exists '{}'",
					categoryDto.getName());
			throw new ExistDataException("Category already exist");
		}

		Category category = mapper.map(categoryDto, Category.class);

		if (ObjectUtils.isEmpty(category.getId())) {
			category.setIsDeleted(false);
			category.setCreatedOn(new Date());
		} else {
			updateCategory(category);
		}

		Category saveCategory = categoryRepo.save(category);

		if (ObjectUtils.isEmpty(saveCategory)) {
			log.error("CategoryServiceImpl : saveCategory() : Failed to save category '{}'",
					categoryDto.getName());
			return false;
		}

		log.info("CategoryServiceImpl : saveCategory() : Category saved successfully. Id={}",
				saveCategory.getId());

		return true;
	}

	private void updateCategory(Category category) {

		Optional<Category> findById = categoryRepo.findById(category.getId());

		if (findById.isPresent()) {

			log.info("CategoryServiceImpl : updateCategory() : Updating category. Id={}",
					category.getId());

			Category existCategory = findById.get();

			category.setCreatedBy(existCategory.getCreatedBy());
			category.setCreatedOn(existCategory.getCreatedOn());
			category.setIsDeleted(existCategory.getIsDeleted());

			// category.setUpdatedBy(1);
			// category.setUpdatedOn(new Date());
		}
	}

	@Override
	public List<CategoryDto> getAllCategory() {

		log.info("CategoryServiceImpl : getAllCategory() : Fetching all categories");

		List<Category> categories = categoryRepo.findAll();

		log.info("CategoryServiceImpl : getAllCategory() : {} categories fetched",
				categories.size());

		return categories.stream()
				.map(cat -> mapper.map(cat, CategoryDto.class))
				.toList();
	}

	@Override
	public List<CategoryResponse> getActiveCategory() {

		log.info("CategoryServiceImpl : getActiveCategory() : Fetching active categories");

		List<Category> categories = categoryRepo.findByIsActiveTrueAndIsDeletedFalse();

		log.info("CategoryServiceImpl : getActiveCategory() : {} active categories fetched",
				categories.size());

		return categories.stream()
				.map(cat -> mapper.map(cat, CategoryResponse.class))
				.toList();
	}

	@Override
	public CategoryDto getCategoryById(Integer id) throws Exception {

		log.info("CategoryServiceImpl : getCategoryById() : Fetching category. Id={}", id);

		Category category = categoryRepo.findByIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found with id=" + id));

		log.info("CategoryServiceImpl : getCategoryById() : Category fetched successfully. Id={}",
				id);

		return mapper.map(category, CategoryDto.class);
	}

	@Override
	public Boolean deleteCategory(Integer id) {

		log.info("CategoryServiceImpl : deleteCategory() : Delete category request. Id={}", id);

		Optional<Category> findByCategory = categoryRepo.findById(id);

		if (findByCategory.isPresent()) {

			Category category = findByCategory.get();
			category.setIsDeleted(true);
			categoryRepo.save(category);

			log.info("CategoryServiceImpl : deleteCategory() : Category deleted successfully. Id={}",
					id);

			return true;
		}

		log.warn("CategoryServiceImpl : deleteCategory() : Category not found. Id={}", id);

		return false;
	}

}
package com.prog.controller;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;

import com.prog.dto.CategoryDto;
import com.prog.dto.CategoryResponse;
import com.prog.endpoint.CategoryEndpoint;
import com.prog.service.CategoryService;
import com.prog.util.CommonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoryEndpoint{

	private final CategoryService categoryService;

	@Override
	public ResponseEntity<?> saveCategory(CategoryDto categoryDto) {
		log.info("CategoryController : saveCategory() : Save category request received. Category={}",categoryDto.getName());
		Boolean saveCategory = categoryService.saveCategory(categoryDto);
		if (saveCategory) {
			log.info("CategoryController : saveCategory() : Category saved successfully. Category={}",categoryDto.getName());
			return CommonUtil.createBuildResponseMessage("saved success", HttpStatus.CREATED);
		}
		log.error("CategoryController : saveCategory() : Failed to save category. Category={}",categoryDto.getName());
		return CommonUtil.createErrorResponseMessage("Category Not saved", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	@Cacheable("allCategory")
	public ResponseEntity<?> getAllCategory() {
		log.info("CategoryController : getAllCategory() : Fetch all categories request received");
		List<CategoryDto> allCategory = categoryService.getAllCategory();
		if (CollectionUtils.isEmpty(allCategory)) {
			log.warn("CategoryController : getAllCategory() : No categories found");
			return ResponseEntity.noContent().build();
		}
		log.info("CategoryController : getAllCategory() : {} categories fetched successfully",allCategory.size());
		return CommonUtil.createBuildResponse(allCategory, HttpStatus.OK);
	}

	@Override
	@Cacheable(value = "activeCategory")
	public ResponseEntity<?> getActiveCategory() {
		log.info("CategoryController : getActiveCategory() : Fetch active categories request received");
		List<CategoryResponse> allCategory = categoryService.getActiveCategory();
		if (CollectionUtils.isEmpty(allCategory)) {
			log.warn("CategoryController : getActiveCategory() : No active categories found");
			return ResponseEntity.noContent().build();
		}
		log.info("CategoryController : getActiveCategory() : {} active categories fetched successfully",allCategory.size());
		return CommonUtil.createBuildResponse(allCategory, HttpStatus.OK);
	}

	@Override
	@Cacheable(value = "categoryById", key = "#id")
	public ResponseEntity<?> getCategortDetailsById(Integer id) throws Exception {
		log.info("CategoryController : getCategoryDetailsById() : Fetch category request. Id={}", id);
		CategoryDto categoryDto = categoryService.getCategoryById(id);
		if (ObjectUtils.isEmpty(categoryDto)) {
			log.warn("CategoryController : getCategoryDetailsById() : Category not found. Id={}", id);
			return CommonUtil.createErrorResponseMessage("Internal Server Error", HttpStatus.NOT_FOUND);
		}
		log.info("CategoryController : getCategoryDetailsById() : Category fetched successfully. Id={}", id);
		return CommonUtil.createBuildResponse(categoryDto, HttpStatus.OK);
	}

	@Override
	@Caching(evict = {
		    @CacheEvict(value = "allCategory", allEntries = true),
		    @CacheEvict(value = "activeCategory", allEntries = true),
		    @CacheEvict(value = "categoryById", key = "#id")
		})
	public ResponseEntity<?> deleteCategoryById(Integer id) {
		log.info("CategoryController : deleteCategoryById() : Delete category request. Id={}", id);
		Boolean deleted = categoryService.deleteCategory(id);
		if (deleted) {
			log.info("CategoryController : deleteCategoryById() : Category deleted successfully. Id={}", id);
			return CommonUtil.createBuildResponse("Category deleted success", HttpStatus.OK);
		}
		log.error("CategoryController : deleteCategoryById() : Failed to delete category. Id={}", id);
		return CommonUtil.createErrorResponseMessage("Category Not deleted", HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
package com.laidekuai.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laidekuai.category.dto.CategoryCreateRequest;
import com.laidekuai.category.dto.CategoryUpdateRequest;
import com.laidekuai.category.entity.Category;
import com.laidekuai.category.mapper.CategoryMapper;
import com.laidekuai.category.service.impl.CategoryServiceImpl;
import com.laidekuai.common.enums.CategoryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 分类服务测试
 *
 * @author Laidekuai Team
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryCreateRequest createRequest;
    private CategoryUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("电子产品");
        testCategory.setParentId(null);
        testCategory.setLevel(1);
        testCategory.setSortOrder(1);
        testCategory.setIconUrl("icon.png");
        testCategory.setStatus(CategoryStatus.ENABLED);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());

        createRequest = new CategoryCreateRequest();
        createRequest.setName("手机");
        createRequest.setParentId(1L);
        createRequest.setSortOrder(1);
        createRequest.setIconUrl("phone.png");

        updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("智能手机");
        updateRequest.setParentId(null);
        updateRequest.setSortOrder(2);
        updateRequest.setIconUrl("smartphone.png");
    }

    @Test
    void testGetCategoryTree_Success() {
        // Given
        Category level1 = new Category();
        level1.setId(1L);
        level1.setName("电子产品");
        level1.setParentId(null);
        level1.setLevel(1);
        level1.setSortOrder(1);
        level1.setStatus(CategoryStatus.ENABLED);

        Category level2 = new Category();
        level2.setId(2L);
        level2.setName("手机");
        level2.setParentId(1L);
        level2.setLevel(2);
        level2.setSortOrder(1);
        level2.setStatus(CategoryStatus.ENABLED);

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(level1, level2));

        // When
        var result = categoryService.getCategoryTree();

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("电子产品", result.getData().get(0).getName());
        assertEquals(1, result.getData().get(0).getChildren().size());
        assertEquals("手机", result.getData().get(0).getChildren().get(0).getName());
    }

    @Test
    void testGetCategoryById_Success() {
        // Given
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);

        // When
        var result = categoryService.getCategoryById(1L);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("电子产品", result.getData().getName());
    }

    @Test
    void testGetCategoryById_NotFound() {
        // Given
        when(categoryMapper.selectById(1L)).thenReturn(null);

        // When
        var result = categoryService.getCategoryById(1L);

        // Then
        assertFalse(result.isSuccess());
    }

    @Test
    void testCreateCategory_Success() {
        // Given
        Category parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setLevel(1);

        createRequest.setParentId(1L);

        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.selectById(1L)).thenReturn(parentCategory);
        when(categoryMapper.insert(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(2L);
            return 1;
        });

        // When
        var result = categoryService.createCategory(createRequest);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2L, result.getData().getId());
        assertEquals(2, result.getData().getLevel());
        verify(categoryMapper, times(1)).insert(any(Category.class));
    }

    @Test
    void testCreateCategory_DuplicateName() {
        // Given
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When
        var result = categoryService.createCategory(createRequest);

        // Then
        assertFalse(result.isSuccess());
        verify(categoryMapper, never()).insert(any(Category.class));
    }

    @Test
    void testCreateCategory_LevelExceeded() {
        // Given
        Category level3Category = new Category();
        level3Category.setId(3L);
        level3Category.setLevel(3);

        createRequest.setParentId(3L);

        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.selectById(3L)).thenReturn(level3Category);

        // When
        var result = categoryService.createCategory(createRequest);

        // Then
        assertFalse(result.isSuccess());
        verify(categoryMapper, never()).insert(any(Category.class));
    }

    @Test
    void testUpdateCategory_Success() {
        // Given
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        // When
        var result = categoryService.updateCategory(1L, updateRequest);

        // Then
        assertTrue(result.isSuccess());
        verify(categoryMapper, times(1)).updateById(any(Category.class));
    }

    @Test
    void testUpdateCategory_CircularReference() {
        // Given
        Category childCategory = new Category();
        childCategory.setId(2L);
        childCategory.setParentId(1L);

        updateRequest.setParentId(2L);

        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(categoryMapper.selectById(2L)).thenReturn(childCategory);

        // When
        var result = categoryService.updateCategory(1L, updateRequest);

        // Then
        assertFalse(result.isSuccess());
        verify(categoryMapper, never()).updateById(any(Category.class));
    }

    @Test
    void testDeleteCategory_Success() {
        // Given
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.countGoodsByCategory(1L)).thenReturn(0L);
        when(categoryMapper.deleteById(1L)).thenReturn(1);

        // When
        var result = categoryService.deleteCategory(1L);

        // Then
        assertTrue(result.isSuccess());
        verify(categoryMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCategory_HasChildren() {
        // Given
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When
        var result = categoryService.deleteCategory(1L);

        // Then
        assertFalse(result.isSuccess());
        verify(categoryMapper, never()).deleteById(any());
    }

    @Test
    void testDeleteCategory_HasGoods() {
        // Given
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.countGoodsByCategory(1L)).thenReturn(5L);

        // When
        var result = categoryService.deleteCategory(1L);

        // Then
        assertFalse(result.isSuccess());
        verify(categoryMapper, never()).deleteById(any());
    }

    @Test
    void testHasCircularReference_True() {
        // Given - 1 -> 2 -> 3，如果把1的父设为3，会形成循环 3 -> 1 -> 2 -> 3
        Category category1 = new Category();
        category1.setId(1L);
        category1.setParentId(null);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setParentId(1L);

        Category category3 = new Category();
        category3.setId(3L);
        category3.setParentId(2L);

        when(categoryMapper.selectById(3L)).thenReturn(category3);
        when(categoryMapper.selectById(2L)).thenReturn(category2);
        when(categoryMapper.selectById(1L)).thenReturn(category1);

        // When
        boolean result = categoryService.hasCircularReference(1L, 3L);

        // Then
        assertTrue(result);
    }

    @Test
    void testHasCircularReference_False() {
        // Given - 不形成循环的情况
        Category category1 = new Category();
        category1.setId(1L);
        category1.setParentId(null);

        when(categoryMapper.selectById(10L)).thenReturn(category1);

        // When
        boolean result = categoryService.hasCircularReference(2L, 10L);

        // Then
        assertFalse(result);
    }
}

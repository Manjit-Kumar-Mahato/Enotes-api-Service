package com.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

import com.prog.entity.User;
import com.prog.util.CommonUtil;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.prog.dto.TodoDto;
import com.prog.dto.TodoDto.StatusDto;
import com.prog.entity.Todo;
import com.prog.enums.TodoStatus;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.TodoRepository;
import com.prog.service.impl.TodoServiceImpl;
import com.prog.util.Validation;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

	@Mock
	private TodoRepository todoRepo;

	@Mock
	private ModelMapper mapper;

	@Mock
	private Validation validation;

	@InjectMocks
	private TodoServiceImpl todoService;

	private TodoDto todoDto;
	private Todo todo;

	@BeforeEach
	public void initialize() {

		todoDto = mock(TodoDto.class);
		todo = mock(Todo.class);
	}

	@Test
	public void testSaveTodo() throws Exception {

		StatusDto statusDto = mock(StatusDto.class);

		when(todoDto.getStatus()).thenReturn(statusDto);

		when(statusDto.getId()).thenReturn(1);

		when(mapper.map(todoDto, Todo.class)).thenReturn(todo);

		when(todoRepo.save(todo)).thenReturn(todo);

		when(todo.getId()).thenReturn(1);

		Boolean result = todoService.saveTodo(todoDto);

		assertTrue(result);

		verify(validation).todoValidation(todoDto);

		verify(mapper).map(todoDto, Todo.class);

		verify(todoDto).getStatus();

		verify(statusDto).getId();

		verify(todo).setStatusId(1);

		verify(todoRepo).save(todo);
	}

	@Test
	public void testSaveTodoFailed() throws Exception {

		StatusDto statusDto = mock(StatusDto.class);

		when(todoDto.getStatus()).thenReturn(statusDto);

		when(statusDto.getId()).thenReturn(1);

		when(mapper.map(todoDto, Todo.class)).thenReturn(todo);

		when(todoRepo.save(todo)).thenReturn(null);

		Boolean result = todoService.saveTodo(todoDto);

		assertFalse(result);

		verify(validation).todoValidation(todoDto);

		verify(mapper).map(todoDto, Todo.class);

		verify(todo).setStatusId(1);

		verify(todoRepo).save(todo);
	}

	@Test
	public void testGetTodoById() throws Exception {

		Integer id = 1;

		when(todoRepo.findById(id)).thenReturn(Optional.of(todo));

		when(mapper.map(todo, TodoDto.class)).thenReturn(todoDto);

		TodoStatus status = TodoStatus.values()[0];

		when(todo.getStatusId()).thenReturn(status.getId());

		TodoDto result = todoService.getTodoById(id);

		assertNotNull(result);
		assertSame(todoDto, result);

		verify(todoRepo).findById(id);

		verify(mapper).map(todo, TodoDto.class);

		verify(todoDto).setStatus(any(StatusDto.class));
	}

	@Test
	public void testGetTodoByIdNotFound() {

		Integer id = 100;

		when(todoRepo.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> todoService.getTodoById(id));

		assertEquals("Todo not Found | Id Invalid", exception.getMessage());

		verify(todoRepo).findById(id);

		verify(mapper, never()).map(any(), eq(TodoDto.class));
	}

	@Test
	public void testGetTodoByIdWithoutMatchingStatus() throws Exception {

		Integer id = 1;

		when(todoRepo.findById(id)).thenReturn(Optional.of(todo));

		when(mapper.map(todo, TodoDto.class)).thenReturn(todoDto);

		when(todo.getStatusId()).thenReturn(-1);

		TodoDto result = todoService.getTodoById(id);

		assertNotNull(result);
		assertSame(todoDto, result);

		verify(todoRepo).findById(id);

		verify(mapper).map(todo, TodoDto.class);

		verify(todoDto, never()).setStatus(any(StatusDto.class));
	}

	@Test
	public void testGetTodoByUser() {

		User user = new User();
		user.setId(2);

		List<Todo> todos = List.of(todo);

		when(todoRepo.findByCreatedBy(2)).thenReturn(todos);

		when(mapper.map(todo, TodoDto.class)).thenReturn(todoDto);

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			List<TodoDto> result = todoService.getTodoByUser();

			assertNotNull(result);
			assertEquals(1, result.size());
			assertSame(todoDto, result.get(0));

			verify(todoRepo).findByCreatedBy(2);

			verify(mapper).map(todo, TodoDto.class);
		}
	}

	@Test
	public void testGetTodoByUserEmpty() {

		User user = new User();
		user.setId(2);

		when(todoRepo.findByCreatedBy(2)).thenReturn(List.of());

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			List<TodoDto> result = todoService.getTodoByUser();

			assertNotNull(result);
			assertTrue(result.isEmpty());

			verify(todoRepo).findByCreatedBy(2);
			verify(mapper, never()).map(any(), eq(TodoDto.class));
		}
	}
}
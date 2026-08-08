package com.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.prog.dto.FavouriteNoteDto;
import com.prog.dto.NotesDto;
import com.prog.dto.NotesDto.CategoryDto;
import com.prog.dto.NotesResponse;
import com.prog.entity.Category;
import com.prog.entity.FileDetails;
import com.prog.entity.FavouriteNote;
import com.prog.entity.Notes;
import com.prog.entity.User;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.CategoryRepository;
import com.prog.repository.FavouriteNoteRepository;
import com.prog.repository.FileRepository;
import com.prog.repository.NotesRepository;
import com.prog.service.impl.NotesServiceImpl;
import com.prog.util.CommonUtil;

@ExtendWith(MockitoExtension.class)
public class NotesServiceTest {

	@Mock
	private NotesRepository notesRepo;

	@Mock
	private ModelMapper mapper;

	@Mock
	private FavouriteNoteRepository favouriteNoteRepo;

	@Mock
	private CategoryRepository categoryRepo;

	@Mock
	private FileRepository fileRepo;

	@Mock
	private Clock clock;
	
	private User user;

	@InjectMocks
	private NotesServiceImpl notesService;

	private Notes notes;

	private Category category;

	private FileDetails fileDetails;

	private NotesDto notesDto;

	@BeforeEach
	public void initialize() {

		user = new User();
		user.setId(1);
		
		category = new Category();
		category.setId(1);

		notes = new Notes();
		notes.setId(1);
		notes.setTitle("Java Notes");
		notes.setDescription("Java notes");
		notes.setCategory(category);
		notes.setIsDeleted(false);

		fileDetails = new FileDetails();
		fileDetails.setId(1);
		fileDetails.setOriginalFileName("java.pdf");
		fileDetails.setPath("test-files/java.pdf");

		notesDto = new NotesDto();
		notesDto.setId(1);
		notesDto.setTitle("Java Notes");
		notesDto.setDescription("Java notes");

		CategoryDto categoryDto = new CategoryDto();
		categoryDto.setId(1);

		notesDto.setCategory(categoryDto);
	}

	@Test
	public void testGetAllNotes() {

		when(notesRepo.findAll()).thenReturn(List.of(notes));

		when(mapper.map(notes, NotesDto.class)).thenReturn(notesDto);

		List<NotesDto> result = notesService.getAllNotes();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertSame(notesDto, result.get(0));

		verify(notesRepo).findAll();
		verify(mapper).map(notes, NotesDto.class);
	}

	@Test
	public void testGetAllNotesEmpty() {

		when(notesRepo.findAll()).thenReturn(List.of());

		List<NotesDto> result = notesService.getAllNotes();

		assertNotNull(result);
		assertTrue(result.isEmpty());

		verify(notesRepo).findAll();
		verify(mapper, never()).map(any(), eq(NotesDto.class));
	}

	@Test
	public void testGetFileDetails() throws Exception {

		Integer id = 1;

		when(fileRepo.findById(id)).thenReturn(Optional.of(fileDetails));

		FileDetails result = notesService.getFileDetails(id);

		assertNotNull(result);
		assertSame(fileDetails, result);

		verify(fileRepo).findById(id);
	}

	@Test
	public void testGetFileDetailsNotFound() {

		Integer id = 100;

		when(fileRepo.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> notesService.getFileDetails(id));

		assertEquals("File is not available", exception.getMessage());

		verify(fileRepo).findById(id);
	}

	@Test
	public void testDownloadFile() throws Exception {

	    Path tempFile = Files.createTempFile("notes-test-", ".txt");

	    byte[] expectedData = "Java Notes".getBytes();

	    Files.write(tempFile, expectedData);

	    FileDetails file = new FileDetails();
	    file.setOriginalFileName("java.txt");
	    file.setPath(tempFile.toString());

	    byte[] result = notesService.downloadFile(file);

	    assertArrayEquals(expectedData, result);

	    tempFile.toFile().deleteOnExit();
	}

	@Test
	public void testSoftDeleteNotes() throws Exception {

		Integer id = 1;

		Instant instant = Instant.parse("2026-08-08T10:00:00Z");

		when(clock.instant()).thenReturn(instant);

		when(clock.getZone()).thenReturn(ZoneOffset.UTC);

		when(notesRepo.findById(id)).thenReturn(Optional.of(notes));

		notesService.softDeleteNotes(id);

		assertTrue(notes.getIsDeleted());
		assertNotNull(notes.getDeletedOn());

		verify(notesRepo).findById(id);
		verify(notesRepo).save(notes);
	}

	@Test
	public void testSoftDeleteNotesNotFound() {

		Integer id = 100;

		when(notesRepo.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> notesService.softDeleteNotes(id));

		assertEquals("Notes id invalid ! Not Found", exception.getMessage());

		verify(notesRepo).findById(id);

		verify(notesRepo, never()).save(any(Notes.class));
	}

	@Test
	public void testRestoreNotes() throws Exception {

		Integer id = 1;

		notes.setIsDeleted(true);

		when(notesRepo.findById(id)).thenReturn(Optional.of(notes));

		notesService.restoreNotes(id);

		assertFalse(notes.getIsDeleted());
		assertNull(notes.getDeletedOn());

		verify(notesRepo).findById(id);
		verify(notesRepo).save(notes);
	}

	@Test
	public void testRestoreNotesNotFound() {

		Integer id = 100;

		when(notesRepo.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> notesService.restoreNotes(id));

		assertEquals("Notes id invalid ! Not Found", exception.getMessage());

		verify(notesRepo).findById(id);

		verify(notesRepo, never()).save(any(Notes.class));
	}

	@Test
	public void testHardDeleteNotes() throws Exception {

		Integer id = 1;

		notes.setIsDeleted(true);

		when(notesRepo.findById(id)).thenReturn(Optional.of(notes));

		notesService.hardDeleteNotes(id);

		verify(notesRepo).findById(id);
		verify(notesRepo).delete(notes);
	}

	@Test
	public void testHardDeleteNotesNotFound() {

		Integer id = 100;

		when(notesRepo.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> notesService.hardDeleteNotes(id));

		assertEquals("Notes not found", exception.getMessage());

		verify(notesRepo).findById(id);

		verify(notesRepo, never()).delete(any(Notes.class));
	}

	@Test
	public void testHardDeleteNotesWithoutSoftDelete() {

		Integer id = 1;

		notes.setIsDeleted(false);

		when(notesRepo.findById(id)).thenReturn(Optional.of(notes));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> notesService.hardDeleteNotes(id));

		assertEquals("Sorry You cant hard delete Directly", exception.getMessage());

		verify(notesRepo).findById(id);

		verify(notesRepo, never()).delete(any(Notes.class));
	}

	@Test
	public void testUnFavoriteNotes() throws Exception {

		Integer favoriteId = 1;

		FavouriteNote favouriteNote = new FavouriteNote();

		when(favouriteNoteRepo.findById(favoriteId)).thenReturn(Optional.of(favouriteNote));

		notesService.unFavoriteNotes(favoriteId);

		verify(favouriteNoteRepo).findById(favoriteId);

		verify(favouriteNoteRepo).delete(favouriteNote);
	}

	@Test
	public void testUnFavoriteNotesNotFound() {

		Integer favoriteId = 100;

		when(favouriteNoteRepo.findById(favoriteId)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> notesService.unFavoriteNotes(favoriteId));

		assertEquals("Favourite Note not Found & Id invalid", exception.getMessage());

		verify(favouriteNoteRepo).findById(favoriteId);

		verify(favouriteNoteRepo, never()).delete(any(FavouriteNote.class));
	}

	@Test
	public void testCopyNotes() throws Exception {

		Integer id = 1;

		when(notesRepo.findById(id)).thenReturn(Optional.of(notes));

		Notes copiedNote = new Notes();
		copiedNote.setId(2);

		when(notesRepo.save(any(Notes.class))).thenReturn(copiedNote);

		Boolean result = notesService.copyNotes(id);

		assertTrue(result);

		verify(notesRepo).findById(id);

		verify(notesRepo).save(any(Notes.class));
	}

	@Test
	public void testCopyNotesNotFound() {

		Integer id = 100;

		when(notesRepo.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> notesService.copyNotes(id));

		assertEquals("Notes id Invalid ! Not Found", exception.getMessage());

		verify(notesRepo).findById(id);

		verify(notesRepo, never()).save(any(Notes.class));
	}

	@Test
	public void testGetAllNotesByUser() {

		Integer pageNo = 0;
		Integer pageSize = 10;

		Page<Notes> page = mock(Page.class);

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			when(notesRepo.findByCreatedByAndIsDeletedFalse(eq(1), any(Pageable.class))).thenReturn(page);

			when(page.get()).thenReturn(List.of(notes).stream());

			when(page.getNumber()).thenReturn(0);

			when(page.getSize()).thenReturn(10);

			when(page.getTotalElements()).thenReturn(1L);

			when(page.getTotalPages()).thenReturn(1);

			when(page.isFirst()).thenReturn(true);

			when(page.isLast()).thenReturn(true);

			when(mapper.map(notes, NotesDto.class)).thenReturn(notesDto);

			NotesResponse result = notesService.getAllNotesByUser(pageNo, pageSize);

			assertNotNull(result);
			assertEquals(1, result.getNotes().size());
			assertEquals(0, result.getPageNo());
			assertEquals(10, result.getPageSize());
			assertEquals(1, result.getTotalElements());
			assertEquals(1, result.getTotalPages());
			assertTrue(result.getIsFirst());
			assertTrue(result.getIsLast());

			verify(notesRepo).findByCreatedByAndIsDeletedFalse(eq(1), any(Pageable.class));

			verify(mapper).map(notes, NotesDto.class);
		}
	}

	@Test
	public void testGetUserRecycleBinNotes() {

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			when(notesRepo.findByCreatedByAndIsDeletedTrue(1)).thenReturn(List.of(notes));

			when(mapper.map(notes, NotesDto.class)).thenReturn(notesDto);

			List<NotesDto> result = notesService.getUserRecycleBinNotes();

			assertNotNull(result);
			assertEquals(1, result.size());
			assertSame(notesDto, result.get(0));

			verify(notesRepo).findByCreatedByAndIsDeletedTrue(1);

			verify(mapper).map(notes, NotesDto.class);
		}
	}

	@Test
	public void testEmptyRecycleBin() {

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			when(notesRepo.findByCreatedByAndIsDeletedTrue(1)).thenReturn(List.of(notes));

			notesService.emptyRecycleBin();

			verify(notesRepo).findByCreatedByAndIsDeletedTrue(1);

			verify(notesRepo).deleteAll(List.of(notes));
		}
	}

	@Test
	public void testEmptyRecycleBinWhenEmpty() {

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			when(notesRepo.findByCreatedByAndIsDeletedTrue(1)).thenReturn(List.of());

			notesService.emptyRecycleBin();

			verify(notesRepo).findByCreatedByAndIsDeletedTrue(1);

			verify(notesRepo, never()).deleteAll(any());
		}
	}

	@Test
	public void testGetUserFavoriteNotes() throws Exception {

		FavouriteNote favouriteNote = new FavouriteNote();

		FavouriteNoteDto favouriteNoteDto = new FavouriteNoteDto();

		when(favouriteNoteRepo.findByUserId(1)).thenReturn(List.of(favouriteNote));

		when(mapper.map(favouriteNote, FavouriteNoteDto.class)).thenReturn(favouriteNoteDto);

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			List<FavouriteNoteDto> result = notesService.getUserFavoriteNotes();

			assertNotNull(result);
			assertEquals(1, result.size());
			assertSame(favouriteNoteDto, result.get(0));

			verify(favouriteNoteRepo).findByUserId(1);

			verify(mapper).map(favouriteNote, FavouriteNoteDto.class);
		}
	}
}
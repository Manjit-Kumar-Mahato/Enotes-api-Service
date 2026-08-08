package com.prog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.prog.dto.FavouriteNoteDto;
import com.prog.dto.NotesDto;
import com.prog.dto.NotesResponse;
import com.prog.entity.FileDetails;
import com.prog.service.NotesService;

@ExtendWith(MockitoExtension.class)
public class NotesControllerTest {

	private MockMvc mockMvc;

	@Mock
	private NotesService notesService;

	@InjectMocks
	private NotesController notesController;

	@BeforeEach
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(notesController).build();
	}

	@Test
	public void testSaveNotes() throws Exception {

		when(notesService.saveNotes(any(String.class), any())).thenReturn(true);

		MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test file".getBytes());

		mockMvc.perform(multipart("/api/v1/notes/").file(file).param("notes", "{\"title\":\"Java Notes\"}"))
				.andExpect(status().isCreated());

		verify(notesService).saveNotes(any(String.class), any());
	}

	@Test
	public void testSaveNotesFailed() throws Exception {

		when(notesService.saveNotes(any(String.class), any())).thenReturn(false);

		MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test file".getBytes());

		mockMvc.perform(multipart("/api/v1/notes/").file(file).param("notes", "{\"title\":\"Java Notes\"}"))
				.andExpect(status().isInternalServerError());

		verify(notesService).saveNotes(any(String.class), any());
	}

	@Test
	public void testDownloadFile() throws Exception {

		FileDetails fileDetails = org.mockito.Mockito.mock(FileDetails.class);

		when(fileDetails.getOriginalFileName()).thenReturn("test.pdf");
		when(notesService.getFileDetails(1)).thenReturn(fileDetails);
		when(notesService.downloadFile(fileDetails)).thenReturn("test file".getBytes());

		mockMvc.perform(get("/api/v1/notes/download/1")).andExpect(status().isOk());

		verify(notesService).getFileDetails(1);
		verify(notesService).downloadFile(fileDetails);
	}

	@Test
	public void testGetAllNotes() throws Exception {

		NotesDto notesDto = org.mockito.Mockito.mock(NotesDto.class);

		when(notesService.getAllNotes()).thenReturn(List.of(notesDto));

		mockMvc.perform(get("/api/v1/notes/")).andExpect(status().isOk());

		verify(notesService).getAllNotes();
	}

	@Test
	public void testGetAllNotesEmpty() throws Exception {

		when(notesService.getAllNotes()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/notes/")).andExpect(status().isNoContent());

		verify(notesService).getAllNotes();
	}

	@Test
	public void testSearchNotes() throws Exception {

		NotesResponse notesResponse = org.mockito.Mockito.mock(NotesResponse.class);

		when(notesService.getNotesByUserSearch(0, 10, "java")).thenReturn(notesResponse);

		mockMvc.perform(get("/api/v1/notes/search").param("key", "java").param("pageNo", "0").param("pageSize", "10"))
				.andExpect(status().isOk());

		verify(notesService).getNotesByUserSearch(0, 10, "java");
	}

	@Test
	public void testGetAllNotesByUser() throws Exception {

		NotesResponse notesResponse = org.mockito.Mockito.mock(NotesResponse.class);

		when(notesService.getAllNotesByUser(0, 10)).thenReturn(notesResponse);

		mockMvc.perform(get("/api/v1/notes/user-notes").param("pageNo", "0").param("pageSize", "10"))
				.andExpect(status().isOk());

		verify(notesService).getAllNotesByUser(0, 10);
	}

	@Test
	public void testDeleteNotes() throws Exception {

		mockMvc.perform(get("/api/v1/notes/delete/1")).andExpect(status().isOk());

		verify(notesService).softDeleteNotes(1);
	}

	@Test
	public void testRestoreNotes() throws Exception {

		mockMvc.perform(get("/api/v1/notes/restore/1")).andExpect(status().isOk());

		verify(notesService).restoreNotes(1);
	}

	@Test
	public void testGetUserRecycleBinNotes() throws Exception {

		NotesDto notesDto = org.mockito.Mockito.mock(NotesDto.class);

		when(notesService.getUserRecycleBinNotes()).thenReturn(List.of(notesDto));

		mockMvc.perform(get("/api/v1/notes/recycle-bin")).andExpect(status().isOk());

		verify(notesService).getUserRecycleBinNotes();
	}

	@Test
	public void testGetUserRecycleBinNotesEmpty() throws Exception {

		when(notesService.getUserRecycleBinNotes()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/notes/recycle-bin")).andExpect(status().isOk());

		verify(notesService).getUserRecycleBinNotes();
	}

	@Test
	public void testHardDeleteNotes() throws Exception {

		mockMvc.perform(delete("/api/v1/notes/delete/1")).andExpect(status().isOk());

		verify(notesService).hardDeleteNotes(1);
	}

	@Test
	public void testEmptyUserRecycleBin() throws Exception {

		mockMvc.perform(delete("/api/v1/notes/delete")).andExpect(status().isOk());

		verify(notesService).emptyRecycleBin();
	}

	@Test
	public void testFavoriteNote() throws Exception {

		mockMvc.perform(get("/api/v1/notes/fav/1")).andExpect(status().isCreated());

		verify(notesService).favoriteNotes(1);
	}

	@Test
	public void testUnFavoriteNote() throws Exception {

		mockMvc.perform(delete("/api/v1/notes/un-fav/1")).andExpect(status().isOk());

		verify(notesService).unFavoriteNotes(1);
	}

	@Test
	public void testGetUserFavoriteNote() throws Exception {
		FavouriteNoteDto favouriteNoteDto = org.mockito.Mockito.mock(FavouriteNoteDto.class);
		when(notesService.getUserFavoriteNotes()).thenReturn(List.of(favouriteNoteDto));
		mockMvc.perform(get("/api/v1/notes/fav-note")).andExpect(status().isOk());
		verify(notesService).getUserFavoriteNotes();
	}

	@Test
	public void testGetUserFavoriteNoteEmpty() throws Exception {
		when(notesService.getUserFavoriteNotes()).thenReturn(List.of());
		mockMvc.perform(get("/api/v1/notes/fav-note")).andExpect(status().isNoContent());
		verify(notesService).getUserFavoriteNotes();
	}

	@Test
	public void testCopyNotes() throws Exception {
		when(notesService.copyNotes(1)).thenReturn(true);
		mockMvc.perform(get("/api/v1/notes/copy/1")).andExpect(status().isCreated());
		verify(notesService).copyNotes(1);
	}

	@Test
	public void testCopyNotesFailed() throws Exception {
		when(notesService.copyNotes(1)).thenReturn(false);
		mockMvc.perform(get("/api/v1/notes/copy/1")).andExpect(status().isInternalServerError());
		verify(notesService).copyNotes(1);
	}
}
package com.prog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prog.dto.FavouriteNoteDto;
import com.prog.dto.NotesDto;
import com.prog.dto.NotesResponse;
import com.prog.entity.FileDetails;
import com.prog.service.NotesService;
import com.prog.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/notes")
public class NotesController {

	@Autowired
	private NotesService notesService;

	@PostMapping("/")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> saveNotes(@RequestParam String notes, @RequestParam(required = false) MultipartFile file)throws Exception {
		log.info("NotesController : saveNotes() : Save notes request received");
		Boolean saveNotes = notesService.saveNotes(notes, file);
		if (saveNotes) {
			log.info("NotesController : saveNotes() : Notes saved successfully");
			return CommonUtil.createBuildResponseMessage("Notes saved success", HttpStatus.CREATED);
		}
		log.error("NotesController : saveNotes() : Failed to save notes");
		return CommonUtil.createErrorResponseMessage("Notes not saved", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@GetMapping("/download/{id}")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<?> downloadFile(@PathVariable Integer id) throws Exception {
		log.info("NotesController : downloadFile() : File download request received. FileId={}", id);
		FileDetails fileDetails = notesService.getFileDetails(id);
		byte[] data = notesService.downloadFile(fileDetails);
		HttpHeaders headers = new HttpHeaders();
		String contentType = CommonUtil.getContentType(fileDetails.getOriginalFileName());
		headers.setContentType(MediaType.parseMediaType(contentType));
		headers.setContentDispositionFormData("attachment", fileDetails.getOriginalFileName());
		log.info("NotesController : downloadFile() : File downloaded successfully. FileId={}", id);
		return ResponseEntity.ok().headers(headers).body(data);
	}

	@GetMapping("/")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<?> getAllNotes() {
		log.info("NotesController : getAllNotes() : Fetch all notes request received");
		List<NotesDto> notes = notesService.getAllNotes();
		if (CollectionUtils.isEmpty(notes)) {
			log.warn("NotesController : getAllNotes() : No notes found");
			return ResponseEntity.noContent().build();
		}
		log.info("NotesController : getAllNotes() : {} notes fetched successfully", notes.size());
		return CommonUtil.createBuildResponse(notes, HttpStatus.OK);
	}

	@GetMapping("/search")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> searchNotes(
			@RequestParam(name = "key", defaultValue = "") String key,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		log.info("NotesController : searchNotes() : Search request received. Keyword={}", key);
		NotesResponse notes = notesService.getNotesByUserSearch(pageNo, pageSize, key);
		log.info("NotesController : searchNotes() : Search completed successfully");
		return CommonUtil.createBuildResponse(notes, HttpStatus.OK);
	}

	@GetMapping("/user-notes")
	@PreAuthorize("hasAnyRole('USER')")
	public ResponseEntity<?> getAllNotesByUser(
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		log.info("NotesController : getAllNotesByUser() : Fetch user notes request received");
		Integer userId = 2;
		NotesResponse notes = notesService.getAllNotesByUser(userId, pageNo, pageSize);
		log.info("NotesController : getAllNotesByUser() : User notes fetched successfully");
		return CommonUtil.createBuildResponse(notes, HttpStatus.OK);
	}

	@GetMapping("/delete/{id}")
	@PreAuthorize("hasAnyRole('USER')")
	public ResponseEntity<?> deleteNotes(@PathVariable Integer id) throws Exception {
		log.info("NotesController : deleteNotes() : Soft delete request received. NoteId={}", id);
		notesService.softDeleteNotes(id);
		log.info("NotesController : deleteNotes() : Note moved to recycle bin successfully. NoteId={}", id);
		return CommonUtil.createBuildResponseMessage("Delete Success", HttpStatus.OK);
	}

	@GetMapping("/restore/{id}")
	@PreAuthorize("hasAnyRole('USER')")
	public ResponseEntity<?> restoreNotes(@PathVariable Integer id) throws Exception {
		log.info("NotesController : restoreNotes() : Restore note request received. NoteId={}", id);
		notesService.restoreNotes(id);
		log.info("NotesController : restoreNotes() : Note restored successfully. NoteId={}", id);
		return CommonUtil.createBuildResponseMessage("Notes restore Success", HttpStatus.OK);
	}

	@GetMapping("/recycle-bin")
	@PreAuthorize("hasAnyRole('USER')")
	public ResponseEntity<?> getUserRecycleBinNotes() throws Exception {
		log.info("NotesController : getUserRecycleBinNotes() : Fetch recycle bin notes request received");
		Integer userId = 2;
		List<NotesDto> notes = notesService.getUserRecycleBinNotes(userId);
		if (CollectionUtils.isEmpty(notes)) {
			log.warn("NotesController : getUserRecycleBinNotes() : No notes found in recycle bin");
			return CommonUtil.createBuildResponseMessage("Notes not avaible in Recycle Bin", HttpStatus.OK);
		}
		log.info("NotesController : getUserRecycleBinNotes() : {} recycle bin notes fetched successfully",notes.size());
		return CommonUtil.createBuildResponse(notes, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasAnyRole('USER')")
	public ResponseEntity<?> hardDeleteNotes(@PathVariable Integer id) throws Exception {
		log.info("NotesController : hardDeleteNotes() : Permanent delete request received. NoteId={}", id);
		notesService.hardDeleteNotes(id);
		log.info("NotesController : hardDeleteNotes() : Note permanently deleted. NoteId={}", id);
		return CommonUtil.createBuildResponseMessage("Delete Success", HttpStatus.OK);
	}

	@DeleteMapping("/delete")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> emptyRecyleBin() throws Exception {
		log.info("NotesController : emptyRecycleBin() : Empty recycle bin request received");
		int userId = 2;
		notesService.emptyRecycleBin(userId);
		log.info("NotesController : emptyRecycleBin() : Recycle bin emptied successfully");
		return CommonUtil.createBuildResponseMessage("Delete Success", HttpStatus.OK);
	}

	@GetMapping("/fav/{noteId}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> favoriteNote(@PathVariable Integer noteId) throws Exception {
		log.info("NotesController : favoriteNote() : Add favourite request received. NoteId={}", noteId);
		notesService.favoriteNotes(noteId);
		log.info("NotesController : favoriteNote() : Note added to favourites successfully. NoteId={}", noteId);
		return CommonUtil.createBuildResponseMessage("Notes added to Favourite", HttpStatus.CREATED);
	}

	@DeleteMapping("/un-fav/{favNotId}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> unFavoriteNote(@PathVariable Integer favNotId) throws Exception {
		log.info("NotesController : unFavoriteNote() : Remove favourite request received. FavouriteId={}", favNotId);
		notesService.unFavoriteNotes(favNotId);
		log.info("NotesController : unFavoriteNote() : Favourite removed successfully. FavouriteId={}", favNotId);
		return CommonUtil.createBuildResponseMessage("Remove Favorite", HttpStatus.OK);
	}

	@GetMapping("/fav-note")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> getUserfavoriteNote() throws Exception {
		log.info("NotesController : getUserFavoriteNote() : Fetch favourite notes request received");
		List<FavouriteNoteDto> userFavoriteNotes = notesService.getUserFavoriteNotes();
		if (CollectionUtils.isEmpty(userFavoriteNotes)) {
			log.warn("NotesController : getUserFavoriteNote() : No favourite notes found");
			return ResponseEntity.noContent().build();
		}

		log.info("NotesController : getUserFavoriteNote() : {} favourite notes fetched successfully",
				userFavoriteNotes.size());

		return CommonUtil.createBuildResponse(userFavoriteNotes, HttpStatus.OK);
	}

	@GetMapping("/copy/{id}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> copyNotes(@PathVariable Integer id) throws Exception {
		log.info("NotesController : copyNotes() : Copy note request received. NoteId={}", id);
		Boolean copyNotes = notesService.copyNotes(id);
		if (copyNotes) {
			log.info("NotesController : copyNotes() : Note copied successfully. NoteId={}", id);
			return CommonUtil.createBuildResponseMessage("Copied success", HttpStatus.CREATED);
		}
		log.error("NotesController : copyNotes() : Failed to copy note. NoteId={}", id);
		return CommonUtil.createErrorResponseMessage("Copy failed ! Try Again", HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
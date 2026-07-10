package com.prog.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import com.prog.dto.FavouriteNoteDto;
import com.prog.dto.NotesDto;
import com.prog.dto.NotesDto.CategoryDto;
import com.prog.dto.NotesDto.FilesDto;
import com.prog.dto.NotesResponse;
import com.prog.entity.Category;
import com.prog.entity.FavouriteNote;
import com.prog.entity.FileDetails;
import com.prog.entity.Notes;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.CategoryRepository;
import com.prog.repository.FavouriteNoteRepository;
import com.prog.repository.FileRepository;
import com.prog.repository.NotesRepository;
import com.prog.service.NotesService;
import com.prog.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class NotesServiceImpl implements NotesService {

	@Autowired
	private NotesRepository notesRepo;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private FavouriteNoteRepository favouriteNoteRepo;

	@Autowired
	private CategoryRepository categoryRepo;

	@Value("${file.upload.path}")
	private String uploadpath;

	@Autowired
	private FileRepository fileRepo;

	@Override
	public Boolean saveNotes(String notes, MultipartFile file) throws Exception {
		log.info("NotesServiceImpl : saveNotes() : Saving notes");
		ObjectMapper ob = new ObjectMapper();
		NotesDto notesDto = ob.readValue(notes, NotesDto.class);

		notesDto.setIsDeleted(false);
		notesDto.setDeletedOn(null);

		// update notes if id is given in request
		if (!ObjectUtils.isEmpty(notesDto.getId())) {
			log.info("NotesServiceImpl : saveNotes() : Updating existing notes. NoteId={}", notesDto.getId());
			updateNotes(notesDto, file);
		}

		// category validation
		checkCategoryExist(notesDto.getCategory());

		Notes notesMap = mapper.map(notesDto, Notes.class);

		FileDetails fileDtls = saveFileDetails(file);

		if (!ObjectUtils.isEmpty(fileDtls)) {
			log.info("NotesServiceImpl : saveNotes() : Saving attachment '{}'", file.getOriginalFilename());
			notesMap.setFileDetails(fileDtls);
		} else {
			if (ObjectUtils.isEmpty(notesDto.getId())) {
				notesMap.setFileDetails(null);
			}
		}

		Notes saveNotes = notesRepo.save(notesMap);
		if (!ObjectUtils.isEmpty(saveNotes)) {
			log.info("NotesServiceImpl : saveNotes() : Notes saved successfully. NoteId={}", saveNotes.getId());
			return true;
		}
		log.error("NotesServiceImpl : saveNotes() : Failed to save notes");
		return false;
	}

	private void updateNotes(NotesDto notesDto, MultipartFile file) throws Exception {

		Notes existNotes = notesRepo.findById(notesDto.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Invalid Notes id"));

		// user not choose any file at update time
		if (ObjectUtils.isEmpty(file)) {
			notesDto.setFileDetails(mapper.map(existNotes.getFileDetails(), FilesDto.class));
		}

	}

	private FileDetails saveFileDetails(MultipartFile file) throws IOException {
		if (!ObjectUtils.isEmpty(file) && !file.isEmpty()) {
			String originalFilename = file.getOriginalFilename();
			log.info("NotesServiceImpl : saveFileDetails() : Uploading file '{}'", originalFilename);
			String extension = FilenameUtils.getExtension(originalFilename);

			List<String> extensionAllow = Arrays.asList("pdf", "xlsx", "jpg", "png", "docx");
			if (!extensionAllow.contains(extension)) {
				throw new IllegalArgumentException("invalid file format ! Upload only .pdf , .xlsx,.jpg");
			}

			String rndString = UUID.randomUUID().toString();
			String uploadfileName = rndString + "." + extension; // sdfsafbhkljsf.pdf

			File saveFile = new File(uploadpath);
			if (!saveFile.exists()) {
				saveFile.mkdir();
			}
			// path : enotesapiservice/notes/java.pdf
			String storePath = uploadpath.concat(uploadfileName);

			// upload file
			long upload = Files.copy(file.getInputStream(), Paths.get(storePath));
			if (upload != 0) {
				FileDetails fileDtls = new FileDetails();
				fileDtls.setOriginalFileName(originalFilename);
				fileDtls.setDisplayFileName(getDisplayName(originalFilename));
				fileDtls.setUploadFileName(uploadfileName);
				fileDtls.setFileSize(file.getSize());
				fileDtls.setPath(storePath);
				FileDetails saveFileDtls = fileRepo.save(fileDtls);
				log.info("NotesServiceImpl : saveFileDetails() : File uploaded successfully '{}'", uploadfileName);
				return saveFileDtls;
			}
		}

		return null;
	}

	private String getDisplayName(String originalFilename) {
		// java_programming_tutorials.pdf
		// java_prog.pdf
		String extension = FilenameUtils.getExtension(originalFilename);
		String fileName = FilenameUtils.removeExtension(originalFilename);

		if (fileName.length() > 8) {
			fileName = fileName.substring(0, 7);
		}
		fileName = fileName + "." + extension;
		return fileName;
	}

	private void checkCategoryExist(CategoryDto category) throws Exception {
		categoryRepo.findById(category.getId()).orElseThrow(() -> new ResourceNotFoundException("category id invalid"));
	}

	@Override
	public List<NotesDto> getAllNotes() {
		log.info("NotesServiceImpl : getAllNotes() : Fetching all notes");
		List<NotesDto> allNotes = notesRepo.findAll().stream().map(note -> mapper.map(note, NotesDto.class)).toList();
		log.info("NotesServiceImpl : getAllNotes() : {} notes fetched", allNotes.size());
		return allNotes;
	}

	@Override
	public byte[] downloadFile(FileDetails fileDetails) throws Exception {
		log.info("NotesServiceImpl : downloadFile() : Downloading file '{}'", fileDetails.getOriginalFileName());
		InputStream io = new FileInputStream(fileDetails.getPath());
		return StreamUtils.copyToByteArray(io);
	}

	@Override
	public FileDetails getFileDetails(Integer id) throws Exception {
		log.info("NotesServiceImpl : getFileDetails() : Fetching file details. FileId={}", id);
		FileDetails fileDtls = fileRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("File is not available"));
		return fileDtls;
	}

	@Override
	public NotesResponse getAllNotesByUser(Integer userId, Integer pageNo, Integer pageSize) {
		// 10 = 5,5 = 2 pages
		log.info("NotesServiceImpl : getAllNotesByUser() : Fetching notes for userId={}", userId);
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Notes> pageNotes = notesRepo.findByCreatedByAndIsDeletedFalse(userId, pageable);

		List<NotesDto> notesDto = pageNotes.get().map(n -> mapper.map(n, NotesDto.class)).toList();

		NotesResponse notes = NotesResponse.builder().notes(notesDto).pageNo(pageNotes.getNumber())
				.pageSize(pageNotes.getSize()).totalElements(pageNotes.getTotalElements())
				.totalPages(pageNotes.getTotalPages()).isFirst(pageNotes.isFirst()).isLast(pageNotes.isLast()).build();
		log.info("NotesServiceImpl : getAllNotesByUser() : {} notes fetched",pageNotes.getTotalElements());
		return notes;
	}

	@Override
	public void softDeleteNotes(Integer id) throws Exception {
		log.info("NotesServiceImpl : softDeleteNotes() : Soft deleting note. NoteId={}", id);
		Notes notes = notesRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notes id invalid ! Not Found"));
		notes.setIsDeleted(true);
		notes.setDeletedOn(LocalDateTime.now());
		notesRepo.save(notes);
		log.info("NotesServiceImpl : softDeleteNotes() : Note moved to recycle bin. NoteId={}", id);
	}

	@Override
	public void restoreNotes(Integer id) throws Exception {
		log.info("NotesServiceImpl : restoreNotes() : Restoring note. NoteId={}", id);
		Notes notes = notesRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notes id invalid ! Not Found"));
		notes.setIsDeleted(false);
		notes.setDeletedOn(null);
		notesRepo.save(notes);
		log.info("NotesServiceImpl : restoreNotes() : Note restored successfully. NoteId={}", id);
	}

	@Override
	public List<NotesDto> getUserRecycleBinNotes(Integer userId) {
		List<Notes> recycleNotes = notesRepo.findByCreatedByAndIsDeletedTrue(userId);
		List<NotesDto> notesDtoList = recycleNotes.stream().map(note -> mapper.map(note, NotesDto.class)).toList();
		return notesDtoList;
	}

	@Override
	public void hardDeleteNotes(Integer id) throws Exception {
		log.info("NotesServiceImpl : hardDeleteNotes() : Permanently deleting note. NoteId={}", id);
		Notes notes = notesRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notes not found"));
		if (notes.getIsDeleted()) {
			notesRepo.delete(notes);
		} else {
			throw new IllegalArgumentException("Sorry You cant hard delete Directly");
		}
		log.info("NotesServiceImpl : hardDeleteNotes() : Note deleted permanently. NoteId={}", id);
	}

	@Override
	public void emptyRecycleBin(int userId) {
		log.info("NotesServiceImpl : emptyRecycleBin() : Empty recycle bin request. UserId={}", userId);
		List<Notes> recycleNotes = notesRepo.findByCreatedByAndIsDeletedTrue(userId);
		if (!CollectionUtils.isEmpty(recycleNotes)) {
			notesRepo.deleteAll(recycleNotes);
		}
		log.info("NotesServiceImpl : emptyRecycleBin() : Recycle bin emptied successfully");
	}

	@Override
	public void favoriteNotes(Integer noteId) throws Exception {
		log.info("NotesServiceImpl : emptyRecycleBin() : Recycle bin emptied successfully");
		int userId = 2;
		Notes notes = notesRepo.findById(noteId)
				.orElseThrow(() -> new ResourceNotFoundException("Noes not Found & Id invalid"));
		FavouriteNote favouriteNote = FavouriteNote.builder().note(notes).userId(userId).build();
		favouriteNoteRepo.save(favouriteNote);
		log.info("NotesServiceImpl : favoriteNotes() : Note added to favourite. NoteId={}", noteId);
	}

	@Override
	public void unFavoriteNotes(Integer favoriteNoteId) throws Exception {
		log.info("NotesServiceImpl : unFavoriteNotes() : Removing favourite. FavouriteId={}", favoriteNoteId);
		FavouriteNote favouriteNote = favouriteNoteRepo.findById(favoriteNoteId)
				.orElseThrow(() -> new ResourceNotFoundException("Favourite Note not Found & Id invalid"));
		favouriteNoteRepo.delete(favouriteNote);
		log.info("NotesServiceImpl : unFavoriteNotes() : Favourite removed successfully");
	}

	@Override
	public List<FavouriteNoteDto> getUserFavoriteNotes() throws Exception {
		int UserId = 2;
		log.info("NotesServiceImpl : getUserFavoriteNotes() : Fetching favourite notes");
		List<FavouriteNote> favouriteNotes = favouriteNoteRepo.findByUserId(UserId);
		return favouriteNotes.stream().map(fn -> mapper.map(fn, FavouriteNoteDto.class)).toList();
	}

	@Override
	public Boolean copyNotes(Integer id) throws Exception {
		log.info("NotesServiceImpl : copyNotes() : Copying note. NoteId={}", id);
		Notes notes = notesRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notes id Invalid ! Not Found"));
		Notes copyNote = Notes.builder().title(notes.getTitle()).description(notes.getDescription())
				.category(notes.getCategory()).isDeleted(false).fileDetails(null).build();
		Notes saveCopyNote = notesRepo.save(copyNote);
		if (!ObjectUtils.isEmpty(saveCopyNote)) {
			log.info("NotesServiceImpl : copyNotes() : Note copied successfully");
			return true;
		}
		log.error("NotesServiceImpl : copyNotes() : Failed to copy note");
		return false;
	}

	@Override
	public NotesResponse getNotesByUserSearch(Integer pageNo, Integer pageSize, String keyword) {
		log.info("NotesServiceImpl : getNotesByUserSearch() : Searching notes. Keyword='{}'", keyword);
		Integer userId = CommonUtil.getLoggedInUser().getId();
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Notes> pageNotes = notesRepo.searchNotes(keyword, userId, pageable);
		List<NotesDto> notesDto = pageNotes.get().map(n -> mapper.map(n, NotesDto.class)).toList();
		NotesResponse notes = NotesResponse.builder().notes(notesDto).pageNo(pageNotes.getNumber())
				.pageSize(pageNotes.getSize()).totalElements(pageNotes.getTotalElements())
				.totalPages(pageNotes.getTotalPages()).isFirst(pageNotes.isFirst()).isLast(pageNotes.isLast()).build();
		log.info("NotesServiceImpl : getNotesByUserSearch() : {} notes found",
		        pageNotes.getTotalElements());
		return notes;
	}

}
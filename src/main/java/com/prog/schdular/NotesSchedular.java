package com.prog.schdular;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.prog.entity.Notes;
import com.prog.repository.NotesRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NotesSchedular {

    private final NotesRepository notesRepo;
    private final Clock clock;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteNotesSchdular() {

        LocalDateTime cutOffDate = LocalDateTime.now(clock).minusDays(7);

        List<Notes> deleteNotes =
                notesRepo.findAllByIsDeletedAndDeletedOnBefore(true, cutOffDate);

        notesRepo.deleteAll(deleteNotes);
    }
}
package com.studyhub.backend.controller;

import com.studyhub.backend.dto.DeckDto;
import com.studyhub.backend.dto.NoteDto;
import com.studyhub.backend.dto.SyncRequest;
import com.studyhub.backend.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    public ResponseEntity<Void> sync(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SyncRequest request) {
        syncService.sync(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/decks")
    public ResponseEntity<List<DeckDto>> getDecks(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DeckDto> decks = syncService.getDecks(userDetails.getUsername());
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/notes")
    public ResponseEntity<List<NoteDto>> getNotes(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NoteDto> notes = syncService.getNotes(userDetails.getUsername());
        return ResponseEntity.ok(notes);
    }
}
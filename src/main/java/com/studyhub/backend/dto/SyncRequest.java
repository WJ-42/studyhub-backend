package com.studyhub.backend.dto;

import java.util.List;

public class SyncRequest {
    private List<DeckDto> decks;
    private List<NoteDto> notes;

    public List<DeckDto> getDecks() { return decks; }
    public void setDecks(List<DeckDto> decks) { this.decks = decks; }

    public List<NoteDto> getNotes() { return notes; }
    public void setNotes(List<NoteDto> notes) { this.notes = notes; }
}
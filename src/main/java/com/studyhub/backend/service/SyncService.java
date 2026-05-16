package com.studyhub.backend.service;

import com.studyhub.backend.dto.CardDto;
import com.studyhub.backend.dto.DeckDto;
import com.studyhub.backend.dto.NoteDto;
import com.studyhub.backend.dto.SyncRequest;
import com.studyhub.backend.entity.Card;
import com.studyhub.backend.entity.Deck;
import com.studyhub.backend.entity.Note;
import com.studyhub.backend.entity.User;
import com.studyhub.backend.repository.CardRepository;
import com.studyhub.backend.repository.DeckRepository;
import com.studyhub.backend.repository.NoteRepository;
import com.studyhub.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SyncService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public SyncService(
            DeckRepository deckRepository,
            CardRepository cardRepository,
            NoteRepository noteRepository,
            UserRepository userRepository) {
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sync(String email, SyncRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (request.getDecks() != null) {
            for (DeckDto deckDto : request.getDecks()) {
                syncDeck(deckDto, user);
            }
        }

        if (request.getNotes() != null) {
            for (NoteDto noteDto : request.getNotes()) {
                syncNote(noteDto, user);
            }
        }
    }

    private void syncDeck(DeckDto deckDto, User user) {
        Optional<Deck> existing = deckRepository
                .findByClientIdAndUserId(deckDto.getClientId(), user.getId());

        Deck deck = existing.orElse(new Deck());
        deck.setName(deckDto.getName());
        deck.setClientId(deckDto.getClientId());
        deck.setUser(user);
        deckRepository.save(deck);

        if (deckDto.getCards() != null) {
            for (CardDto cardDto : deckDto.getCards()) {
                syncCard(cardDto, deck);
            }
        }
    }

    private void syncCard(CardDto cardDto, Deck deck) {
        Optional<Card> existing = cardRepository
                .findByClientIdAndDeckId(cardDto.getClientId(), deck.getId());

        Card card = existing.orElse(new Card());
        card.setClientId(cardDto.getClientId());
        card.setFront(cardDto.getFront());
        card.setBack(cardDto.getBack());
        card.setDeck(deck);
        cardRepository.save(card);
    }

    private void syncNote(NoteDto noteDto, User user) {
        Optional<Note> existing = noteRepository
                .findByClientIdAndUserId(noteDto.getClientId(), user.getId());

        Note note = existing.orElse(new Note());
        note.setClientId(noteDto.getClientId());
        note.setName(noteDto.getName());
        note.setContent(noteDto.getContent());
        note.setFileType(noteDto.getFileType());
        note.setUser(user);
        noteRepository.save(note);
    }

    public List<DeckDto> getDecks(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return deckRepository.findByUserId(user.getId())
                .stream()
                .map(this::toDeckDto)
                .collect(Collectors.toList());
    }

    public List<NoteDto> getNotes(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return noteRepository.findByUserId(user.getId())
                .stream()
                .map(this::toNoteDto)
                .collect(Collectors.toList());
    }

    private DeckDto toDeckDto(Deck deck) {
        DeckDto dto = new DeckDto();
        dto.setId(deck.getId());
        dto.setClientId(deck.getClientId());
        dto.setName(deck.getName());
        dto.setCreatedAt(deck.getCreatedAt());
        dto.setUpdatedAt(deck.getUpdatedAt());
        dto.setCards(deck.getCards()
                .stream()
                .map(this::toCardDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private CardDto toCardDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setClientId(card.getClientId());
        dto.setFront(card.getFront());
        dto.setBack(card.getBack());
        dto.setCreatedAt(card.getCreatedAt());
        return dto;
    }

    private NoteDto toNoteDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setClientId(note.getClientId());
        dto.setName(note.getName());
        dto.setContent(note.getContent());
        dto.setFileType(note.getFileType());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }
}
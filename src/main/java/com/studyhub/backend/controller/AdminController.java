package com.studyhub.backend.controller;

import com.studyhub.backend.entity.User;
import com.studyhub.backend.repository.DeckRepository;
import com.studyhub.backend.repository.NoteRepository;
import com.studyhub.backend.repository.UserRepository;
import com.studyhub.backend.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminController(
            JwtUtil jwtUtil,
            UserRepository userRepository,
            DeckRepository deckRepository,
            NoteRepository noteRepository,
            PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.noteRepository = noteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Validates that the request carries a valid admin JWT
    private boolean isAdminRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        return jwtUtil.isTokenValid(token) && jwtUtil.isAdminToken(token);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid admin credentials"));
        }

        String token = jwtUtil.generateAdminToken(username);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpServletRequest request) {
        if (!isAdminRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required"));
        }

        List<Map<String, Object>> users = userRepository.findAll().stream()
            .map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "email", u.getEmail(),
                "displayName", u.getDisplayName(),
                "createdAt", u.getCreatedAt().toString()
            ))
            .toList();

        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdminRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required"));
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        }

        // Delete related data first to avoid foreign key constraint violations
        deckRepository.findByUserId(id).forEach(deck -> deckRepository.delete(deck));
        noteRepository.findByUserId(id).forEach(note -> noteRepository.delete(note));
        userRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        if (!isAdminRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required"));
        }

        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Password must be at least 6 characters"));
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}

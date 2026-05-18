package com.studyhub.backend.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/extract")
public class ExtractController {

    // Max characters we send to the AI to avoid blowing the token limit
    private static final int MAX_TEXT_LENGTH = 12000;

    @PostMapping("/file")
    public ResponseEntity<?> extractFromFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        String filename = file.getOriginalFilename() != null
            ? file.getOriginalFilename().toLowerCase()
            : "";

        try {
            String text;
            if (filename.endsWith(".pdf")) {
                text = extractPdf(file);
            } else if (filename.endsWith(".docx")) {
                text = extractDocx(file);
            } else if (filename.endsWith(".txt")) {
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Unsupported file type. Please upload a PDF, DOCX, or TXT file."));
            }

            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No readable text found in the file."));
            }

            // Truncate to avoid token limit issues
            if (trimmed.length() > MAX_TEXT_LENGTH) {
                trimmed = trimmed.substring(0, MAX_TEXT_LENGTH);
            }

            return ResponseEntity.ok(Map.of("text", trimmed));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to read the file: " + e.getMessage()));
        }
    }

    @PostMapping("/url")
    public ResponseEntity<?> extractFromUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No URL provided"));
        }

        // Basic URL validation
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Please provide a full URL starting with http:// or https://"));
        }

        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; StudyHub/1.0; +https://study-hub-aston-hack11.vercel.app)")
                .timeout(10000)
                .get();

            // Remove elements that are unlikely to contain useful study content
            doc.select("nav, header, footer, script, style, noscript, iframe, " +
                       ".nav, .navbar, .header, .footer, .sidebar, .menu, .advertisement, " +
                       ".cookie, .banner, .popup, [role=navigation], [role=banner]").remove();

            String text = doc.body().text().trim();

            if (text.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No readable content found at that URL."));
            }

            if (text.length() > MAX_TEXT_LENGTH) {
                text = text.substring(0, MAX_TEXT_LENGTH);
            }

            return ResponseEntity.ok(Map.of("text", text));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "Could not fetch that URL. Make sure it is publicly accessible."));
        }
    }

    private String extractPdf(MultipartFile file) throws IOException {
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    // Extracts text from DOCX by treating it as a ZIP and reading word/document.xml.
    // This avoids the heavy Apache POI dependency while still giving clean results.
    private String extractDocx(MultipartFile file) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    String xml = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                    // <w:t> tags contain the actual text runs in DOCX XML
                    return xml
                        .replaceAll("<w:t[^>]*>", " ")
                        .replaceAll("<[^>]+>", "")
                        .replaceAll("&amp;", "&")
                        .replaceAll("&lt;", "<")
                        .replaceAll("&gt;", ">")
                        .replaceAll("&quot;", "\"")
                        .replaceAll("\\s+", " ")
                        .trim();
                }
            }
        }
        return "";
    }
}

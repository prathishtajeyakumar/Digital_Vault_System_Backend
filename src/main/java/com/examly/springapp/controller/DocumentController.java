package com.examly.springapp.controller;

import com.examly.springapp.model.Document;
import com.examly.springapp.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(originPatterns = "*")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentTitle") String documentTitle,
            @RequestParam("category") String category) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is required"));
            }
            if (documentTitle == null || documentTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Document title is required"));
            }
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
            }

            Document uploadedDoc = documentService.uploadFile(file, documentTitle.trim(), category.trim());
            return ResponseEntity.ok(uploadedDoc);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Internal server error: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        try {
            Optional<Document> docOpt = documentService.getDocumentById(id);
            if (docOpt.isPresent()) {
                Document doc = docOpt.get();
                java.nio.file.Path filePath = java.nio.file.Paths.get(doc.getFilePath());
                byte[] data = java.nio.file.Files.readAllBytes(filePath);
    
                // Detect mime type
                String contentType = java.nio.file.Files.probeContentType(filePath);
                if (contentType == null) contentType = "application/octet-stream";
    
                // Extract original file extension
                String originalFileName = doc.getFileName();
                String extension = "";
                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex >= 0) {
                    extension = originalFileName.substring(dotIndex); // includes the dot
                }
    
                // Use documentTitle + original extension as download filename
                String downloadFileName = doc.getDocumentTitle().replaceAll("[^a-zA-Z0-9\\-_ ]", "_") + extension;
                // .header("Content-Disposition", "attachment; filename=\"" + downloadFileName + "\"")

    
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + downloadFileName + "\"")
                        .header("Content-Type", contentType)
                        .body(data);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    


    @GetMapping
    public List<Document> getAllDocuments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {
        if (search != null && !search.trim().isEmpty()) {
            return documentService.searchDocuments(search);
        }
        if (sort != null && !sort.trim().isEmpty()) {
            return documentService.getSortedDocuments(sort);
        }
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            Optional<Document> doc = documentService.getDocumentById(id);
            if (doc.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to delete document: " + e.getMessage())
            );
        }
    }
}

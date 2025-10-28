package com.examly.springapp.service.impl;

import com.examly.springapp.model.Document;
import com.examly.springapp.repository.DocumentRepository;
import com.examly.springapp.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    private static final String UPLOAD_DIR = "uploads";

    @Override
    public Document uploadFile(MultipartFile file, String documentTitle, String category) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Validate file size (10MB limit)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("File size exceeds 10MB limit");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Save file
        Files.copy(file.getInputStream(), filePath);

        // Create and save Document
        Document doc = new Document(documentTitle, category, originalFilename, filePath.toString());
        return documentRepository.save(doc);
    }

    @Override
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    @Override
    public List<Document> searchDocuments(String title) {
        if (title == null || title.trim().isEmpty()) {
            return getAllDocuments();
        }
        return documentRepository.findByDocumentTitleContainingIgnoreCase(title.trim());
    }
    
    @Override
    public List<Document> getSortedDocuments(String sortOrder) {
        if ("desc".equalsIgnoreCase(sortOrder)) {
            return documentRepository.findAllByOrderByDocumentTitleDesc();
        }
        return documentRepository.findAllByOrderByDocumentTitleAsc();
    }

    @Override
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    @Override
    public void deleteDocument(Long id) {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isPresent()) {
            Document doc = docOpt.get();
            // Delete physical file
            try {
                Path filePath = Paths.get(doc.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                // Log error but continue with database deletion
                System.err.println("Failed to delete physical file: " + e.getMessage());
            }
            // Delete from database
            documentRepository.deleteById(id);
        }
    }
}

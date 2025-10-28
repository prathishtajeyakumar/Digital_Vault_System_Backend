package com.examly.springapp.service;

import com.examly.springapp.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface DocumentService {
    Document uploadFile(MultipartFile file, String documentTitle, String category) throws IOException;
    List<Document> getAllDocuments();
    List<Document> searchDocuments(String title);
    List<Document> getSortedDocuments(String sortOrder);
    Optional<Document> getDocumentById(Long id);
    void deleteDocument(Long id);
}

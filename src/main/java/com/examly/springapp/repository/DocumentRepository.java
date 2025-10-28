package com.examly.springapp.repository;

import com.examly.springapp.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query("SELECT d FROM Document d WHERE LOWER(d.documentTitle) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Document> findByDocumentTitleContainingIgnoreCase(@Param("title") String title);
    
    List<Document> findAllByOrderByDocumentTitleAsc();
    List<Document> findAllByOrderByDocumentTitleDesc();
}

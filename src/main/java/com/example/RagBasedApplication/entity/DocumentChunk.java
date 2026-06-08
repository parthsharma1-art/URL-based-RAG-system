package com.example.RagBasedApplication.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "document_chunks")
public class DocumentChunk {
    @Id
    private String id;
    private String content;
    private String sourceUrl;
    private List<Double> embedding;
    private Map<String, Object> metadata;
}

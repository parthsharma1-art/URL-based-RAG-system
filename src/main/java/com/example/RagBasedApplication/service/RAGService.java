package com.example.RagBasedApplication.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.example.RagBasedApplication.controller.ChatController.ChatResponse;
import com.example.RagBasedApplication.entity.DocumentChunk;
import com.example.RagBasedApplication.repository.DocumentChunkRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGService {

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;
    private final GeminiService geminiService;

    public ChatResponse chat(String question) {
        log.info("Processing chat question: {}", question);
        List<Double> questionEmbedding = embeddingService.embed(question);
        log.info("Question embedding generated");
        List<DocumentChunk> relevantChunks = findTopKSimilar(questionEmbedding, 5);
        log.info("Found {} relevant chunks", relevantChunks.size());

        String context = buildContext(relevantChunks);
        log.info("Context built");
        String answer = geminiService.generateResponse(context, question);
        log.info("Response generated");

        List<ChatResponse.Source> sources = relevantChunks.stream()
                .map(chunk -> ChatResponse.Source.builder()
                        .url(chunk.getSourceUrl())
                        .chunk(chunk.getContent())
                        .build())
                .collect(Collectors.toList());

        return ChatResponse.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }

    private String buildContext(List<DocumentChunk> chunks) {
        return chunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n---\n"));
    }

    public List<DocumentChunk> findTopKSimilar(List<Double> queryVector, int k) {

        List<DocumentChunk> chunks = documentChunkRepository.findAll();

        return chunks.stream()
                .sorted((a, b) -> Double.compare(
                        cosineSimilarity(b.getEmbedding(), queryVector),
                        cosineSimilarity(a.getEmbedding(), queryVector)))
                .limit(k)
                .toList();
    }

    private double cosineSimilarity(
            List<Double> v1,
            List<Double> v2) {
        double dot = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

}

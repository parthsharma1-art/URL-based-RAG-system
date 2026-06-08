package com.example.RagBasedApplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public List<Double> embed(String text) {
        log.debug("Generating embedding for text of length {}", text.length());
        float[] floatEmbedding = embeddingModel.embed(text);
        List<Double> doubleEmbedding = new ArrayList<>();
        for (float f : floatEmbedding) {
            doubleEmbedding.add((double) f);
        }
        return doubleEmbedding;
    }
}

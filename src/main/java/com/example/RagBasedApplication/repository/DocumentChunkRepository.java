package com.example.RagBasedApplication.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.RagBasedApplication.entity.DocumentChunk;



@Repository
public interface DocumentChunkRepository
                extends MongoRepository<DocumentChunk, String> {

        void deleteBySourceUrl(String sourceUrl);

        @Aggregation(pipeline = {
                        "{ '$vectorSearch': { 'index': 'vector_index', 'queryVector': ?0, 'path': 'embedding', 'numCandidates': 100, 'limit': ?1 } }"
        })
        List<DocumentChunk> findTopKSimilar(List<Double> queryVector, int k);
}

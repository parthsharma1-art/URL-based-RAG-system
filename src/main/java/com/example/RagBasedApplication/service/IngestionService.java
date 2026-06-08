package com.example.RagBasedApplication.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import com.example.RagBasedApplication.entity.DocumentChunk;
import com.example.RagBasedApplication.repository.DocumentChunkRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    public void ingestUrls(List<String> urls) {
        log.info("Starting ingestion for {} URLs", urls.size());
        for (String url : urls) {
            try {
                ingestUrl(url);
            } catch (Exception e) {
                log.error("Failed to ingest URL: {}", url, e);
            }
        }
        log.info("Ingestion completed!");
    }

    private void ingestUrl(String url) throws IOException {
        log.info("Processing URL: {}", url);
        
        // Scrape and clean text using Jsoup
        String cleanText = scrapeAndCleanText(url);
        String title = extractTitle(url);
        
        // Use LangChain4j to split the document
        Document document = Document.from(cleanText);
        document.metadata().put("source_url", url);
        document.metadata().put("title", title);
        
        List<TextSegment> segments = DocumentSplitters.recursive(1000, 200).split(document);

        // Delete existing chunks for this URL to avoid duplicates
        documentChunkRepository.deleteBySourceUrl(url);

        // Save chunks with embeddings
        for (TextSegment segment : segments) {
            List<Double> embedding = embeddingService.embed(segment.text());
            DocumentChunk documentChunk = DocumentChunk.builder()
                    .content(segment.text())
                    .sourceUrl(url)
                    .embedding(embedding)
                    .metadata(Map.of("title", title))
                    .build();
            documentChunkRepository.save(documentChunk);
        }

        log.info("Saved {} chunks for URL: {}", segments.size(), url);
    }

    private String scrapeAndCleanText(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        // Remove unwanted elements
        doc.select("nav, header, footer, script, style, noscript, iframe").remove();
        // Get all paragraphs and headings
        org.jsoup.select.Elements elements = doc.select("h1, h2, h3, h4, p");
        StringBuilder sb = new StringBuilder();
        for (org.jsoup.nodes.Element element : elements) {
            sb.append(element.text()).append("\n");
        }
        return sb.toString().trim();
    }

    private String extractTitle(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.title();
    }
}

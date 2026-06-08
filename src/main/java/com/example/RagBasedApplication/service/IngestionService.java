package com.example.RagBasedApplication.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import java.net.URL;

import com.example.RagBasedApplication.dto.SuccessDTO;
import com.example.RagBasedApplication.entity.DocumentChunk;
import com.example.RagBasedApplication.repository.DocumentChunkRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    public SuccessDTO ingestUrls(List<String> urls) {
        log.info("Starting ingestion for {} URLs", urls.size());
        int successCount = 0;
        int failureCount = 0;
        for (String url : urls) {
            try {
                ingestUrl(url);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to ingest URL: {}", url, e);
            }
        }
        return SuccessDTO.of(
                String.format(
                        "Ingestion completed. Success=%d, Failed=%d",
                        successCount,
                        failureCount));
    }

    private void ingestUrl(String url) throws IOException {
        log.info("Processing URL: {}", url);

        URLConnection connection = new URL(url).openConnection();
        String contentType = connection.getContentType();
        if (contentType == null) {
            throw new RuntimeException("Unable to determine content type");
        }
        log.info("Content type :{}", contentType);
        ParsedContent parsedContent;
        if (url.contains("drive.google.com")) {
            parsedContent = processGoogleDrivePdf(url);
        } else if (contentType.contains("text/html")) {
            parsedContent = processHtml(url);
        } else if (contentType.contains("application/pdf")) {
            parsedContent = processPdf(url);
        } else if (contentType.contains("wordprocessingml")) {
            parsedContent = processDocx(url);
        } else if (contentType.contains("text/plain")) {
            parsedContent = processText(url);
        } else {
            throw new RuntimeException("Unsupported content type: " + contentType);
        }
        String cleanText = parsedContent.getContent();
        log.info("Clean text length : {}", cleanText.length());
        log.info("Clean text : {}", cleanText);
        log.info("Clean text length : {}", cleanText.length());
        if (cleanText.isBlank()) {
            throw new RuntimeException("No text extracted from URL");
        }

        // Use LangChain4j to split the document
        Document document = Document.from(parsedContent.getContent());
        document.metadata().put("source_url", url);
        document.metadata().put("title", parsedContent.getTitle());

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
                    .metadata(Map.of("title", parsedContent.getTitle()))
                    .build();
            documentChunkRepository.save(documentChunk);
        }

        log.info("Saved {} chunks for URL: {}", segments.size(), url);
    }

    private ParsedContent processHtml(String url) throws IOException {
        log.info("Processing HTML");
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        String title = doc.title();
        doc.select(
                "nav,header,footer,script,style,noscript,iframe")
                .remove();
        Elements elements = doc.select("h1,h2,h3,h4,p");
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            sb.append(element.text())
                    .append("\n");
        }

        return new ParsedContent(
                title,
                sb.toString().trim());
    }

    private ParsedContent processPdf(String url) throws IOException {
        log.info("Processing PDF");

        byte[] pdfBytes = new URL(url).openStream().readAllBytes();
        try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);
            return new ParsedContent("PDF Document", text);
        }
    }

    private ParsedContent processDocx(String url)
            throws IOException {
        log.info("Processing DOCX");

        try (
                InputStream inputStream = new URL(url).openStream();
                XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            return new ParsedContent(
                    "DOCX Document",
                    text);
        }
    }

    private ParsedContent processText(String url)
            throws IOException {
        log.info("Processing TEXt");

        String text = new String(
                new URL(url)
                        .openStream()
                        .readAllBytes(),
                StandardCharsets.UTF_8);
        return new ParsedContent(
                "Text Document",
                text);
    }

    private ParsedContent processGoogleDrivePdf(String url) throws IOException {
        log.info("Processing google drive pdf");
        String fileId = url.split("/d/")[1].split("/")[0];
        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
        byte[] pdfBytes = new URL(downloadUrl).openStream().readAllBytes();
        try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);
            return new ParsedContent("PDF Document", text);
        }
    }

    @Data
    @AllArgsConstructor
    public static class ParsedContent {
        private String title;
        private String content;
    }
}

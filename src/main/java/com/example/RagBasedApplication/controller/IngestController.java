package com.example.RagBasedApplication.controller;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RagBasedApplication.dto.SuccessDTO;
import com.example.RagBasedApplication.service.IngestionService;

import java.util.List;

@RestController
@RequestMapping("/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final IngestionService ingestionService;

    @PostMapping
    public SuccessDTO ingest(@RequestBody URls urlsRequest) {
        ingestionService.ingestUrls(urlsRequest.getUrls());
        return SuccessDTO.of("Ingestion completed");
    }

    @Data
    public static class URls{
        private List<String> urls;
    }
}

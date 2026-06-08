package com.example.RagBasedApplication.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.google-ai.gemini.api-key}")
    private String googleAiApiKey;

    @Value("${langchain4j.google-ai.gemini.model-name:gemini-2.5-flash}")
    private String geminiModelName;

    @Bean
    public ChatModel chatLanguageModel() {
        return GoogleGenAiChatModel.builder()
                .apiKey(googleAiApiKey)
                .modelName(geminiModelName)
                .build();
    }
}

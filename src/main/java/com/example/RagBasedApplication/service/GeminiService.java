package com.example.RagBasedApplication.service;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final ChatModel chatLanguageModel;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an AI assistant for Aether AI.

            You must answer ONLY using the provided context.

            Rules:
            - Do not invent information
            - If answer is not in context, say: "I could not find that information in the available documentation."
            - Be concise and professional
            - Mention policy section if available

            Context:
            %s

            Question:
            %s

            Answer:""";

    public String generateResponse(String context, String question) {
        log.debug("Generating response with context length: {} for question: {}", context.length(), question);
        String fullPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, context, question);
        ChatResponse response = chatLanguageModel.chat(new UserMessage(fullPrompt));
        return response.aiMessage().text();
    }
}

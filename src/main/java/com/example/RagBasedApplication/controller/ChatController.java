package com.example.RagBasedApplication.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RagBasedApplication.service.RAGService;
import com.example.RagBasedApplication.service.UserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final RAGService ragService;

    private final UserDetailsService userService;

    @PostMapping
    public ChatResponse chat(HttpServletRequest request, @RequestBody ChatRequest chatRequest) {
        String ipAddress = getClientIp(request);
        log.info("Ip Address is :{}", ipAddress);
        ChatResponse response = ragService.chat(chatRequest.getQuestion());
        userService.getOrCreateUserDetails(ipAddress, chatRequest.getQuestion(), response.getAnswer());
        return response;
    }

    public String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @Data
    public static class ChatRequest {
        private String question;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatResponse {
        private String answer;
        private List<Source> sources;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Source {
            private String url;
            private String chunk;
        }
    }

}

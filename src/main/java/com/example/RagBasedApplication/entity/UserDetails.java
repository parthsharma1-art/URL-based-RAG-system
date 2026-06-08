package com.example.RagBasedApplication.entity;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.RagBasedApplication.service.UserDetailsService.Location;

import lombok.Data;

@Data
@Document(collection = "userDetails")
public class UserDetails {
    @Id
    private String id;
    private String ipAddress;
    private Location location;
    private List<Chat> chats;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    public static class Chat {
        private String message;
        private String response;
        private Instant createdAt;
    }
}

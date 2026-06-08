package com.example.RagBasedApplication.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.RagBasedApplication.entity.UserDetails;

@Repository
public interface UserDetailsRepository extends MongoRepository<UserDetails, String> {

    public Optional<UserDetails> findByIpAddress(String ipAddress);

}

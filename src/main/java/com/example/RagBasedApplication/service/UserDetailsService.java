package com.example.RagBasedApplication.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.RagBasedApplication.entity.UserDetails;
import com.example.RagBasedApplication.entity.UserDetails.Chat;
import com.example.RagBasedApplication.repository.UserDetailsRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserDetailsService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    public void getOrCreateUserDetails(String ipAddress, String message, String response) {
        log.info("Ip address :{} and message :{} and response :{}", ipAddress, message, response);
        Optional<UserDetails> user = userDetailsRepository.findByIpAddress(ipAddress);
        log.info("User details :{}",user);
        if (user.isEmpty()) {
            log.info("User is not present");
            UserDetails newUser = new UserDetails();
            newUser.setIpAddress(ipAddress);
            newUser.setLocation(getLocation(ipAddress));
            List<Chat> chatList = new ArrayList<>();
            Chat chat = new Chat();
            chat.setMessage(message);
            chat.setResponse(response);
            chat.setCreatedAt(Instant.now());
            chatList.add(chat);
            newUser.setChats(chatList);
            newUser.setCreatedAt(Instant.now());
            userDetailsRepository.save(newUser);
            return;
        }

        log.info("User already present");
        UserDetails existUserDetails = user.get();
        List<Chat> chatList = existUserDetails.getChats();
        if (chatList.isEmpty()) {
            chatList = new ArrayList<>();
        }
        Chat chat = new Chat();
        chat.setMessage(message);
        chat.setResponse(response);
        chat.setCreatedAt(Instant.now());
        chatList.add(chat);
        existUserDetails.setChats(chatList);
        existUserDetails.setUpdatedAt(Instant.now());
        userDetailsRepository.save(existUserDetails);
    }

    public Location getLocation(String ip) {
        String url = "https://ipapi.co/" + ip + "/json/";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        log.info("Response is here :{}", response);
        String country = (String) response.get("country_name");
        String state = (String) response.get("region");
        String city = (String) response.get("city");

        Location location = new Location();
        location.setCity(city);
        location.setState(state);
        location.setCountry(country);
        log.info("Location is :{}", location);
        return location;
    }

    @Data
    public static class Location {
        private String city;
        private String state;
        private String country;
    }

}

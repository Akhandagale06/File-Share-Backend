package com.aditya.File.share.spring.controller;

import com.aditya.File.share.spring.documents.UserCredits;
import com.aditya.File.share.spring.dto.ProfileDTO;
import com.aditya.File.share.spring.repository.UserCreditsRepo;
import com.aditya.File.share.spring.service.ProfileService;
import com.aditya.File.share.spring.service.UserCreditsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class ClerkWebhookController {
    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/clerk")
    public ResponseEntity<String> handleClerkWebhook(@RequestHeader("svix-id") String svixId,
                                                     @RequestHeader("svix-timestamp") String svixTimestamp,
                                                     @RequestHeader("svix-signature") String svixSignature,
                                                     @RequestBody String payload) {
        try{
          boolean isValid =verifyWebhookSignature(svixId,svixTimestamp,svixSignature,payload);
          if(!isValid){
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Signature");
          }

          ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            String eventType =rootNode.get("type").asText();

            switch (eventType){
                case "user.created":
                    handleUserCreated(rootNode.path("data"));
                    break;
                case "user.updated":
                    handleUserUpdated(rootNode.path("data"));
                    break;
                case "user.deleted":
                    handleUserDeleted(rootNode.path("data"));
                    break;


            }
            return ResponseEntity.status(HttpStatus.OK).build();



        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    private void handleUserDeleted(JsonNode data) {
        String clerkId =   data.path("id").asText();
        profileService.deleteProfile(clerkId);
    }

    private void handleUserUpdated(JsonNode data) {
        String clerkId = data.path( "id").asText();
        String email ="";
        JsonNode emailAddresses = data.path("email_addresses");
        if(emailAddresses.isArray() && !emailAddresses.isEmpty()){
            email = emailAddresses.get(0).path("email_address").asText();

        }
        String firstName = data.path("first_name").asText("");
        String lastName = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        ProfileDTO updatedProfile=  ProfileDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .photoUrl(photoUrl)
                .build();
      updatedProfile=  profileService.updateProfile(updatedProfile);
      if(updatedProfile == null){
          handleUserCreated(data);
      }

    }

    private void handleUserCreated(JsonNode data) {
        String clerkId = data.path( "id").asText();

        String email ="";
        JsonNode emailAddresses = data.path("email_addresses");
        if(emailAddresses.isArray() && emailAddresses.size()>0){
            email = emailAddresses.get(0).path("email_address").asText();

        }
        String firstName = data.path("first_name").asText("");
        String lastName = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");
      ProfileDTO newProfile=  ProfileDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .photoUrl(photoUrl)
                .build();
      profileService.createProfile(newProfile);
  userCreditsService.createInitialCredits(clerkId);



    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        //validate the signatuer
        return true;
    }
}


package com.aditya.File.share.spring.service;

import com.aditya.File.share.spring.documents.ProfileDocument;
import com.aditya.File.share.spring.dto.ProfileDTO;
import com.aditya.File.share.spring.repository.ProfileRepo;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor

public class ProfileService {
    private final ProfileRepo profileRepo;

    public ProfileDTO createProfile(ProfileDTO profileDTO) {

        if(profileRepo.existsByClerkId(profileDTO.getClerkId())) {
           return updateProfile(profileDTO);
        }

        var existingByEmail = profileRepo.findByEmail(profileDTO.getEmail());
        if (existingByEmail.isPresent()) {
            ProfileDocument profile = existingByEmail.get();
            profile.setClerkId(profileDTO.getClerkId());
            if(profileDTO.getFirstName() != null) profile.setFirstName(profileDTO.getFirstName());
            if(profileDTO.getLastName() != null) profile.setLastName(profileDTO.getLastName());
            if(profileDTO.getPhotoUrl() != null) profile.setPhotoUrl(profileDTO.getPhotoUrl());
            profile = profileRepo.save(profile);
            return ProfileDTO.builder()
                   .id(profile.getId())
                   .clerkId(profile.getClerkId())
                   .email(profile.getEmail())
                   .firstName(profile.getFirstName())
                   .lastName(profile.getLastName())
                   .photoUrl(profile.getPhotoUrl())
                   .credits(profile.getCredits())
                   .createdAt(profile.getCreatedAt())
                   .build();
        }

        ProfileDocument profile=  ProfileDocument.builder()
                .clerkId(profileDTO.getClerkId())
                .email(profileDTO.getEmail())
                .firstName(profileDTO.getFirstName())
                .lastName(profileDTO.getLastName())
                .photoUrl(profileDTO.getPhotoUrl())
                .credits(10)
                .createdAt(Instant.now())
                .build();

              profile= profileRepo.save(profile);


        return    ProfileDTO.builder()
                   .id(profile.getId())
                   .clerkId(profile.getClerkId())
                   .email(profile.getEmail())
                   .firstName(profile.getFirstName())
                   .lastName(profile.getLastName())
                   .photoUrl(profile.getPhotoUrl())
                   .credits(profile.getCredits())
                   .createdAt(profile.getCreatedAt())
                   .build();
    }

    public ProfileDTO updateProfile(ProfileDTO profileDTO) {
       ProfileDocument existingProfile=profileRepo.findByClerkId(profileDTO.getClerkId());
       if(existingProfile != null){
           //update fields

           if(profileDTO.getEmail()!=null && !profileDTO.getEmail().isEmpty()){
               existingProfile.setEmail(profileDTO.getEmail());

           }
           if(profileDTO.getFirstName()!=null && !profileDTO.getFirstName().isEmpty()){
               existingProfile.setFirstName(profileDTO.getFirstName());
           }
           if(profileDTO.getLastName()!=null && !profileDTO.getLastName().isEmpty()){
               existingProfile.setLastName(profileDTO.getLastName());
           }
           if(profileDTO.getPhotoUrl()!=null && !profileDTO.getPhotoUrl().isEmpty()){
               existingProfile.setPhotoUrl(profileDTO.getPhotoUrl());
           }
            profileRepo.save(existingProfile);

        return  ProfileDTO.builder()
                   .id(existingProfile.getId())
                   .clerkId(existingProfile.getClerkId())
                   .email(existingProfile.getEmail())
                   .firstName(existingProfile.getFirstName())
                   .lastName(existingProfile.getLastName())
                   .photoUrl(existingProfile.getPhotoUrl())
                   .credits(existingProfile.getCredits())
                   .createdAt(existingProfile.getCreatedAt())
                   .build();

       }
       return null;
    }

    public boolean existsByClerkId(String clerkId) {
        return profileRepo.existsByClerkId(clerkId);
    }

    public void deleteProfile(String clerkId) {
      ProfileDocument existingProfile= profileRepo.findByClerkId(clerkId);
      if(existingProfile != null){
          profileRepo.delete(existingProfile);
      }
    }
    public ProfileDocument getCurrentProfile() {
        if(SecurityContextHolder.getContext().getAuthentication()==null) {
            throw new UsernameNotFoundException("User Not Authenticated");
        }
        String clerkId=SecurityContextHolder.getContext().getAuthentication().getName();
        ProfileDocument profile = profileRepo.findByClerkId(clerkId);
        
        if (profile == null) {
            // Lazy-load to instantly resolve auth issues if Clerk Webhook is delayed/blocked
            ProfileDTO newProfile = ProfileDTO.builder()
                .clerkId(clerkId)
                .email("missing-email-" + clerkId)
                .firstName("New")
                .lastName("Account")
                .photoUrl("")
                .build();
            createProfile(newProfile);
            profile = profileRepo.findByClerkId(clerkId);
        }
        
        return profile;
    }

}

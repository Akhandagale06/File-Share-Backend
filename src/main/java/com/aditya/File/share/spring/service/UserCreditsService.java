

package com.aditya.File.share.spring.service;

import com.aditya.File.share.spring.documents.UserCredits;
import com.aditya.File.share.spring.documents.ProfileDocument;
import com.aditya.File.share.spring.repository.UserCreditsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditsService {

    private final UserCreditsRepo userCreditsRepo;
    private final ProfileService profileService;

    // Create initial credits for new user
    public UserCredits createInitialCredits(String clerkId) {
        UserCredits userCredits = UserCredits.builder()
                .clerkId(clerkId)
                .credits(10)
                .plan("FREE")
                .build();

        return userCreditsRepo.save(userCredits);
    }

    // Get credits by clerkId
    public UserCredits getUserCredits(String clerkId) {
        return userCreditsRepo.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));
    }

    // 🔥 FIXED: Safe version (no NullPointerException)
    public UserCredits getUserCredits() {

        ProfileDocument profile = profileService.getCurrentProfile();
        return getUserCredits(profile.getClerkId());
    }

    // Check if user has enough credits
    public Boolean hasEnoughCredits(int requiredCredits) {
        UserCredits userCredits = getUserCredits();
        return userCredits.getCredits() >= requiredCredits;
    }

    // Consume 1 credit
    public UserCredits consumeCredit() {
        UserCredits userCredits = getUserCredits();

        if (userCredits.getCredits() <= 0) {
            throw new RuntimeException("Not enough credits");
        }

        userCredits.setCredits(userCredits.getCredits() - 1);
        return userCreditsRepo.save(userCredits);
    }

    // Add credits (for payments/subscriptions)
    public UserCredits addCredits(String clerkId, Integer creditsToAdd, String plan) {
        UserCredits userCredits = userCreditsRepo.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));

        userCredits.setCredits(userCredits.getCredits() + creditsToAdd);
        userCredits.setPlan(plan);

        return userCreditsRepo.save(userCredits);
    }
}
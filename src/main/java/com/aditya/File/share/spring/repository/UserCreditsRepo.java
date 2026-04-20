package com.aditya.File.share.spring.repository;

import com.aditya.File.share.spring.documents.UserCredits;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserCreditsRepo extends MongoRepository<UserCredits, String> {

        Optional<UserCredits> findByClerkId(String clerkId);

}

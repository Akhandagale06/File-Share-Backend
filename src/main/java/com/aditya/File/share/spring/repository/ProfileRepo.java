package com.aditya.File.share.spring.repository;

import com.aditya.File.share.spring.documents.ProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProfileRepo extends MongoRepository<ProfileDocument,String> {

    Optional<ProfileDocument> findByEmail(String email);
     ProfileDocument findByClerkId(String clerkId);

    Boolean existsByClerkId(String clerkId);
}

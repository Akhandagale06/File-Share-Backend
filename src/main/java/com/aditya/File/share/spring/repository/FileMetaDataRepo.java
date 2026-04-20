package com.aditya.File.share.spring.repository;

import com.aditya.File.share.spring.documents.FileMetaDataDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileMetaDataRepo extends MongoRepository<FileMetaDataDoc, String> {

    List<FileMetaDataDoc> findByClerkId(String clerkId);
   Long countByClerkId(String clerkId);
}

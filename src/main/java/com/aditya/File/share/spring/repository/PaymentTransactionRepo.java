package com.aditya.File.share.spring.repository;

import com.aditya.File.share.spring.documents.PaymentTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentTransactionRepo extends MongoRepository<PaymentTransaction, String> {

    List<PaymentTransaction> findByClerkId(String clerkId);
    List<PaymentTransaction> findByClerkIdOrderByTransactionDateDesc(String clerkId);
    List<PaymentTransaction> findByClerkIdAndStatusOrderByTransactionDateDesc(String clerkId, String status);
}

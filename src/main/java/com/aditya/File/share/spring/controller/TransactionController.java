package com.aditya.File.share.spring.controller;

import com.aditya.File.share.spring.documents.PaymentTransaction;
import com.aditya.File.share.spring.documents.ProfileDocument;
import com.aditya.File.share.spring.repository.PaymentTransactionRepo;
import com.aditya.File.share.spring.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final PaymentTransactionRepo paymentTransactionRepo;
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getUserTransactions(){
       ProfileDocument currentProfile= profileService.getCurrentProfile();
       String clerkId = currentProfile.getClerkId();

      List<PaymentTransaction> transactions= paymentTransactionRepo.findByClerkIdAndStatusOrderByTransactionDateDesc(clerkId,"SUCCESS");
    return  ResponseEntity.ok(transactions);
    }
}

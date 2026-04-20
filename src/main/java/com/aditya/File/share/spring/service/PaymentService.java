package com.aditya.File.share.spring.service;

import com.aditya.File.share.spring.documents.PaymentTransaction;
import com.aditya.File.share.spring.documents.ProfileDocument;
import com.aditya.File.share.spring.dto.PaymentDTO;
import com.aditya.File.share.spring.dto.PaymentVerificationDTO;
import com.aditya.File.share.spring.dto.ProfileDTO;
import com.aditya.File.share.spring.repository.PaymentTransactionRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Formatter;

import static java.lang.Long.toHexString;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final PaymentTransactionRepo paymentTransactionRepo;

    @Value("${razorpay.key.id}")
    private  String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private  String razorpaySecret;

    public PaymentDTO createOrder(PaymentDTO paymentDTO) {
        try{
           ProfileDocument currentProfile = profileService.getCurrentProfile();
           String clerkId=currentProfile.getClerkId();
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpaySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", paymentDTO.getAmount());
            orderRequest.put("currency", paymentDTO.getCurrency());
            orderRequest.put("receipt","order_"+System.currentTimeMillis());

            Order order =razorpayClient.orders.create(orderRequest);
            String orderId=order.get("id");

            // create pending transaction record
            PaymentTransaction transaction =PaymentTransaction.builder()
                    .clerkId(clerkId)
                    .orderId(orderId)
                    .planId(paymentDTO.getPlanId())
                    .amount(paymentDTO.getAmount())
                    .currency(paymentDTO.getCurrency())
                    .status("PENDING")
                    .transactionDate(LocalDateTime.now())
                    .userEmail(currentProfile.getEmail())
                    .userName(currentProfile.getFirstName()+" " + currentProfile.getLastName())
                    .build();

            paymentTransactionRepo.save(transaction);

            return PaymentDTO.builder()
                    .orderId(orderId)
                    .success(true)
                    .message("Order created successfully")
                    .build();

        }catch (Exception e){
          return PaymentDTO.builder()
                  .success(false)
                  .message("error creating order"+e.getMessage())
                  .build();
        }
    }

    public PaymentDTO verifyPayment(PaymentVerificationDTO request){
        try{
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            String clerkId=currentProfile.getClerkId();

        String data= request.getRazorpay_order_id()+ "|" +request.getRazorpay_payment_id();
       String generatedSignature = generateHmacSha256Signature(data,razorpaySecret);

       if(!generatedSignature.equals(request.getRazorpay_signature())){
           updateTransactionStatus(request.getRazorpay_order_id(),"FAILED",request.getRazorpay_payment_id(),null);
           return PaymentDTO.builder()
                   .success(false)
                   .message("Payment Signature verification failed..")
                   .build();
       }
       //credits based on plan
            int creditsToAdd =0;
       String plan ="BASIC";
       switch (request.getPlanId()){
           case "premium":
               creditsToAdd=500;
               plan="PREMIUM";
               break;
           case "ultimate":
               creditsToAdd=5000;
               plan="ULTIMATE";
               break;
       }

       if(creditsToAdd > 0){
         userCreditsService.addCredits(clerkId,creditsToAdd,plan);
         updateTransactionStatus(request.getRazorpay_order_id(),"SUCCESS",request.getRazorpay_payment_id(),creditsToAdd);
         return PaymentDTO.builder()
                 .success(true)
                 .message("Payment verification successfully and credits added..")
                 .credits(userCreditsService.getUserCredits(clerkId).getCredits())
                 .build();
       }
       else {
           updateTransactionStatus(request.getRazorpay_order_id(),"FAILED",request.getRazorpay_payment_id(),null);
           return PaymentDTO.builder()
                   .success(false)
                   .message("Invalid plan selected")
                   .build();
       }

        }catch (Exception e){
           try {
               updateTransactionStatus(request.getRazorpay_order_id(),"ERROR",request.getRazorpay_payment_id(),null);

           }
           catch (Exception e2){
               throw new RuntimeException(e2);
           }
           return PaymentDTO.builder()
                   .success(false)
                   .message("Error verifying payment .."+e.getMessage())
                   .build();
        }
    }

    private String generateHmacSha256Signature(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKey =new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        Mac mac=Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] hmacData = mac.doFinal(data.getBytes());
        return toHexString(hmacData);


    }

    private String toHexString(byte[] hmacData) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : hmacData) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

    private void updateTransactionStatus(String razorpayOrderId, String status, String razorpayPaymentId, Integer creditsToAdd) {
        paymentTransactionRepo.findAll().stream()
                .filter(t -> t.getOrderId() !=null && t.getOrderId().equals(razorpayOrderId))
                .findFirst()
                .map(transaction->{
                    transaction.setStatus(status);
                    transaction.setPaymentId(razorpayPaymentId);
                    if(creditsToAdd !=null){
                        transaction.setCreditsAdded(creditsToAdd);
                    }
                    return paymentTransactionRepo.save(transaction);
                })
                .orElse(null);

    }

}

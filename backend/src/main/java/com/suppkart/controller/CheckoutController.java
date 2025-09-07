package com.suppkart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.CheckoutRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.PaymentResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.exception.OrderException;
import com.suppkart.exception.PaymentException;
import com.suppkart.model.entity.User;
import com.suppkart.security.CustomUserDetailsService.UserPrincipal;
import com.suppkart.service.CheckoutService;
import com.suppkart.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/checkout")
@Validated
public class CheckoutController {
    
    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);
    
    @Autowired
    private CheckoutService checkoutService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Initiate checkout process
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> checkout(
            @Valid @RequestBody CheckoutRequest checkoutRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Starting checkout for user: {}", userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            PaymentResponse paymentResponse = checkoutService.processCheckout(checkoutRequest, user);
            
            return ResponseEntity.ok(
                ApiResponse.success("Checkout initiated successfully", paymentResponse)
            );
            
        } catch (OrderException e) {
            logger.error("Order error during checkout for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (PaymentException e) {
            logger.error("Payment error during checkout for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (BusinessException e) {
            logger.error("Business error during checkout for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error during checkout for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Checkout failed. Please try again.")
            );
        }
    }
}

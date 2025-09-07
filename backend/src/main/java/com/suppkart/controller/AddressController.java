package com.suppkart.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.AddressRequest;
import com.suppkart.dto.response.AddressResponse;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.Address;
import com.suppkart.model.entity.User;
import com.suppkart.security.CustomUserDetailsService.UserPrincipal;
import com.suppkart.service.AddressService;
import com.suppkart.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/addresses")
@Validated
public class AddressController {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get all addresses for the current user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Getting addresses for user: {}", userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            List<Address> addresses = addressService.getUserAddresses(user);
            
            List<AddressResponse> addressResponses = addresses.stream()
                    .map(this::convertToAddressResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                ApiResponse.success("Addresses retrieved successfully", addressResponses)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error getting addresses for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error getting addresses for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve addresses")
            );
        }
    }
    
    /**
     * Get address by ID
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Getting address {} for user: {}", addressId, userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            Address address = addressService.getAddressById(addressId, user)
                    .orElseThrow(() -> new BusinessException("ADDRESS_NOT_FOUND", 
                                      "Address not found with id: " + addressId));
            
            AddressResponse addressResponse = convertToAddressResponse(address);
            
            return ResponseEntity.ok(
                ApiResponse.success("Address retrieved successfully", addressResponse)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error getting address {} for user {}: {}", 
                        addressId, userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error getting address {} for user {}: {}", 
                        addressId, userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve address")
            );
        }
    }
    
    /**
     * Add new address
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddressRequest addressRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Adding new address for user: {}", userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            Address address = addressService.addAddress(addressRequest, user);
            
            AddressResponse addressResponse = convertToAddressResponse(address);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Address added successfully", addressResponse)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error adding address for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error adding address for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to add address")
            );
        }
    }
    
    /**
     * Update existing address
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest addressRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Updating address {} for user: {}", addressId, userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            Address address = addressService.updateAddress(addressId, addressRequest, user);
            
            AddressResponse addressResponse = convertToAddressResponse(address);
            
            return ResponseEntity.ok(
                ApiResponse.success("Address updated successfully", addressResponse)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error updating address {} for user {}: {}", 
                        addressId, userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error updating address {} for user {}: {}", 
                        addressId, userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to update address")
            );
        }
    }
    
    /**
     * Delete address
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Deleting address {} for user: {}", addressId, userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            addressService.deleteAddress(addressId, user);
            
            return ResponseEntity.ok(
                ApiResponse.success("Address deleted successfully", null)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error deleting address {} for user {}: {}", 
                        addressId, userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error deleting address {} for user {}: {}", 
                        addressId, userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to delete address")
            );
        }
    }
    
    /**
     * Set address as default
     */
    @PutMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Setting address {} as default for user: {}", addressId, userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            Address address = addressService.setDefaultAddress(addressId, user);
            
            AddressResponse addressResponse = convertToAddressResponse(address);
            
            return ResponseEntity.ok(
                ApiResponse.success("Default address set successfully", addressResponse)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error setting default address {} for user {}: {}", 
                        addressId, userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error setting default address {} for user {}: {}", 
                        addressId, userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to set default address")
            );
        }
    }
    
    /**
     * Get default address
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Getting default address for user: {}", userPrincipal.getUsername());
        
        try {
            User user = userService.getUserById(userPrincipal.getId());
            Address address = addressService.getDefaultAddress(user)
                    .orElseThrow(() -> new BusinessException("DEFAULT_ADDRESS_NOT_FOUND", 
                                      "No default address found"));
            
            AddressResponse addressResponse = convertToAddressResponse(address);
            
            return ResponseEntity.ok(
                ApiResponse.success("Default address retrieved successfully", addressResponse)
            );
            
        } catch (BusinessException e) {
            logger.error("Business error getting default address for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error getting default address for user {}: {}", 
                        userPrincipal.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve default address")
            );
        }
    }
    
    // Helper methods
    
    private AddressResponse convertToAddressResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setAddressId(address.getAddressId());
        response.setFirstName(address.getFirstName());
        response.setLastName(address.getLastName());
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setPhone(address.getPhone());
        response.setIsDefault(address.getIsDefault());
        response.setLabel(address.getLabel());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());
        return response;
    }
}

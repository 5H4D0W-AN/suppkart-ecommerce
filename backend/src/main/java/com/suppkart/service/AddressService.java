package com.suppkart.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suppkart.dto.request.AddressRequest;
import com.suppkart.exception.BusinessException;
import com.suppkart.model.entity.Address;
import com.suppkart.model.entity.User;
import com.suppkart.repository.AddressRepository;

@Service
@Transactional
public class AddressService {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    
    @Autowired
    private AddressRepository addressRepository;
    
    /**
     * Get all addresses for a user
     * @param user the user
     * @return List<Address>
     */
    @Transactional(readOnly = true)
    public List<Address> getUserAddresses(User user) {
        logger.debug("Getting addresses for user: {}", user.getUserId());
        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
    }
    
    /**
     * Get address by ID and user
     * @param addressId the address ID
     * @param user the user
     * @return Optional<Address>
     */
    @Transactional(readOnly = true)
    public Optional<Address> getAddressById(Long addressId, User user) {
        logger.debug("Getting address {} for user: {}", addressId, user.getUserId());
        return addressRepository.findByAddressIdAndUser(addressId, user);
    }
    
    /**
     * Get user's default address
     * @param user the user
     * @return Optional<Address>
     */
    @Transactional(readOnly = true)
    public Optional<Address> getDefaultAddress(User user) {
        logger.debug("Getting default address for user: {}", user.getUserId());
        return addressRepository.findByUserAndIsDefaultTrue(user);
    }
    
    /**
     * Add new address for user
     * @param addressRequest the address request
     * @param user the user
     * @return Address
     * @throws BusinessException if validation fails
     */
    public Address addAddress(AddressRequest addressRequest, User user) {
        logger.info("Adding new address for user: {}", user.getUserId());
        
        try {
            // If this is being set as default, unset other default addresses
            if (Boolean.TRUE.equals(addressRequest.getIsDefault())) {
                unsetDefaultAddresses(user);
            }
            
            Address address = new Address();
            mapAddressRequestToEntity(addressRequest, address);
            address.setUser(user);
            
            Address savedAddress = addressRepository.save(address);
            logger.info("Successfully added address {} for user: {}", savedAddress.getAddressId(), user.getUserId());
            
            return savedAddress;
            
        } catch (Exception e) {
            logger.error("Error adding address for user {}: {}", user.getUserId(), e.getMessage(), e);
            throw new BusinessException("ADDRESS_CREATION_FAILED", "Failed to add address: " + e.getMessage());
        }
    }
    
    /**
     * Update existing address
     * @param addressId the address ID
     * @param addressRequest the address request
     * @param user the user
     * @return Address
     * @throws BusinessException if address not found or validation fails
     */
    public Address updateAddress(Long addressId, AddressRequest addressRequest, User user) {
        logger.info("Updating address {} for user: {}", addressId, user.getUserId());
        
        Address address = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new BusinessException("ADDRESS_NOT_FOUND", "Address not found with id: " + addressId));
        
        try {
            // If this is being set as default, unset other default addresses
            if (Boolean.TRUE.equals(addressRequest.getIsDefault()) && !address.getIsDefault()) {
                unsetDefaultAddresses(user);
            }
            
            mapAddressRequestToEntity(addressRequest, address);
            
            Address updatedAddress = addressRepository.save(address);
            logger.info("Successfully updated address {} for user: {}", addressId, user.getUserId());
            
            return updatedAddress;
            
        } catch (Exception e) {
            logger.error("Error updating address {} for user {}: {}", addressId, user.getUserId(), e.getMessage(), e);
            throw new BusinessException("ADDRESS_UPDATE_FAILED", "Failed to update address: " + e.getMessage());
        }
    }
    
    /**
     * Delete address
     * @param addressId the address ID
     * @param user the user
     * @throws BusinessException if address not found
     */
    public void deleteAddress(Long addressId, User user) {
        logger.info("Deleting address {} for user: {}", addressId, user.getUserId());
        
        Address address = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new BusinessException("ADDRESS_NOT_FOUND", "Address not found with id: " + addressId));
        
        try {
            boolean wasDefault = address.getIsDefault();
            addressRepository.delete(address);
            
            // If the deleted address was default, set another address as default
            if (wasDefault) {
                Optional<Address> nextAddress = addressRepository.findFirstByUserOrderByCreatedAtAsc(user);
                if (nextAddress.isPresent()) {
                    nextAddress.get().setIsDefault(true);
                    addressRepository.save(nextAddress.get());
                    logger.info("Set address {} as new default for user: {}", nextAddress.get().getAddressId(), user.getUserId());
                }
            }
            
            logger.info("Successfully deleted address {} for user: {}", addressId, user.getUserId());
            
        } catch (Exception e) {
            logger.error("Error deleting address {} for user {}: {}", addressId, user.getUserId(), e.getMessage(), e);
            throw new BusinessException("ADDRESS_DELETE_FAILED", "Failed to delete address: " + e.getMessage());
        }
    }
    
    /**
     * Set address as default
     * @param addressId the address ID
     * @param user the user
     * @return Address
     * @throws BusinessException if address not found
     */
    public Address setDefaultAddress(Long addressId, User user) {
        logger.info("Setting address {} as default for user: {}", addressId, user.getUserId());
        
        Address address = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new BusinessException("ADDRESS_NOT_FOUND", "Address not found with id: " + addressId));
        
        try {
            // Unset all other default addresses for this user
            unsetDefaultAddresses(user);
            
            // Set this address as default
            address.setIsDefault(true);
            Address updatedAddress = addressRepository.save(address);
            
            logger.info("Successfully set address {} as default for user: {}", addressId, user.getUserId());
            return updatedAddress;
            
        } catch (Exception e) {
            logger.error("Error setting address {} as default for user {}: {}", addressId, user.getUserId(), e.getMessage(), e);
            throw new BusinessException("ADDRESS_DEFAULT_FAILED", "Failed to set default address: " + e.getMessage());
        }
    }
    
    /**
     * Validate address for checkout
     * @param addressId the address ID
     * @param user the user
     * @return Address
     * @throws BusinessException if address not found or invalid
     */
    @Transactional(readOnly = true)
    public Address validateAddressForCheckout(Long addressId, User user) {
        logger.debug("Validating address {} for checkout for user: {}", addressId, user.getUserId());
        
        Address address = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new BusinessException("ADDRESS_NOT_FOUND", "Address not found with id: " + addressId));
        
        // Validate required fields
        if (address.getFirstName() == null || address.getFirstName().trim().isEmpty()) {
            throw new BusinessException("ADDRESS_VALIDATION_FAILED", "Address first name is required");
        }
        if (address.getLastName() == null || address.getLastName().trim().isEmpty()) {
            throw new BusinessException("ADDRESS_VALIDATION_FAILED", "Address last name is required");
        }
        if (address.getAddressLine1() == null || address.getAddressLine1().trim().isEmpty()) {
            throw new BusinessException("ADDRESS_VALIDATION_FAILED", "Address line 1 is required");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            throw new BusinessException("ADDRESS_VALIDATION_FAILED", "City is required");
        }
        if (address.getState() == null || address.getState().trim().isEmpty()) {
            throw new BusinessException("ADDRESS_VALIDATION_FAILED", "State is required");
        }
        if (address.getPostalCode() == null || address.getPostalCode().trim().isEmpty()) {
            throw new BusinessException("ADDRESS_VALIDATION_FAILED", "Postal code is required");
        }
        if (address.getCountry() == null || address.getCountry().trim().isEmpty()) {
            throw new BusinessException("ADDRESS_VALIDATION_FAILED", "Country is required");
        }
        
        return address;
    }
    
    /**
     * Count addresses for user
     * @param user the user
     * @return long count
     */
    @Transactional(readOnly = true)
    public long countUserAddresses(User user) {
        return addressRepository.countByUser(user);
    }
    
    /**
     * Check if user has any addresses
     * @param user the user
     * @return boolean
     */
    @Transactional(readOnly = true)
    public boolean hasAddresses(User user) {
        return addressRepository.existsByUser(user);
    }
    
    // Private helper methods
    
    /**
     * Unset all default addresses for a user
     * @param user the user
     */
    private void unsetDefaultAddresses(User user) {
        logger.debug("Unsetting default addresses for user: {}", user.getUserId());
        List<Address> defaultAddresses = addressRepository.findByUserAndIsDefaultTrue(user)
                .map(List::of)
                .orElse(List.of());
        
        for (Address address : defaultAddresses) {
            address.setIsDefault(false);
            addressRepository.save(address);
        }
    }
    
    /**
     * Map AddressRequest to Address entity
     * @param request the address request
     * @param address the address entity
     */
    private void mapAddressRequestToEntity(AddressRequest request, Address address) {
        address.setFirstName(request.getFirstName());
        address.setLastName(request.getLastName());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPhone(request.getPhone());
        address.setLabel(request.getLabel());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
    }
}

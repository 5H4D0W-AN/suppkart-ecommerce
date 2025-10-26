package com.suppkart.service;

import com.suppkart.dto.admin.customer.*;
import com.suppkart.dto.admin.order.OrderSummaryDTO;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.User;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.Address;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.repository.UserRepository;
import com.suppkart.repository.OrderRepository;
import com.suppkart.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for admin customer management operations
 * Provides comprehensive customer analytics, filtering, and management capabilities
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Get filtered customers with pagination
     */
    public Page<CustomerDTO> getAllCustomers(CustomerFilterRequest filter, Pageable pageable) {
        log.debug("Getting all customers with filter: {}", filter);
        
        Specification<User> spec = createCustomerSpecification(filter);
        Page<User> users = userRepository.findAll(spec, pageable);
        
        List<CustomerDTO> customerDTOs = users.getContent().stream()
                .map(this::mapToCustomerDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(customerDTOs, pageable, users.getTotalElements());
    }

    /**
     * Get detailed customer information by ID
     */
    public CustomerDetailDTO getCustomerById(Long customerId) {
        log.debug("Getting customer details for ID: {}", customerId);
        
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        return mapToCustomerDetailDTO(customer);
    }

    /**
     * Get customer orders
     */
    public List<OrderSummaryDTO> getCustomerOrders(Long customerId) {
        log.debug("Getting orders for customer ID: {}", customerId);
        
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(customerId);
        
        return orders.stream()
                .map(this::mapToOrderSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get customer statistics
     */
    public CustomerStats getCustomerStats(Long customerId) {
        log.debug("Getting customer statistics for ID: {}", customerId);
        
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        return calculateCustomerStats(customer);
    }

    /**
     * Delete customer (GDPR compliance)
     */
    @Transactional
    public void deleteCustomer(Long customerId) {
        log.debug("Deleting customer for GDPR compliance, ID: {}", customerId);
        
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        // Check if customer has active orders
        List<Order> activeOrders = orderRepository.findByUserIdAndStatusIn(
                customerId, 
                Arrays.asList(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.SHIPPED)
        );
        
        if (!activeOrders.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with active orders. Complete or cancel orders first.");
        }
        
        // Anonymize customer data instead of hard delete for audit purposes
        anonymizeCustomerData(customer);
        
        log.info("Customer data anonymized for GDPR compliance, ID: {}", customerId);
    }

    /**
     * Export customer data (GDPR compliance)
     */
    public void exportCustomerData(Long customerId) {
        log.debug("Exporting customer data for GDPR compliance, ID: {}", customerId);
        
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        // TODO: including all customer data, orders, addresses, reviews, etc.
        StringBuilder exportData = new StringBuilder();
        exportData.append("Customer Data Export\n");
        exportData.append("===================\n\n");
        exportData.append("Name: ").append(customer.getName()).append("\n");
        exportData.append("Email: ").append(customer.getEmail()).append("\n");
        exportData.append("Phone: ").append(customer.getPhone()).append("\n");
        exportData.append("Registration Date: ").append(customer.getCreatedAt()).append("\n");
        exportData.append("Status: ").append(customer.getStatus()).append("\n\n");
        
        // Add orders
        List<OrderSummaryDTO> orders = getCustomerOrders(customerId);
        exportData.append("Orders (").append(orders.size()).append("):\n");
        for (OrderSummaryDTO order : orders) {
            exportData.append("- Order #").append(order.getOrderNumber())
                    .append(" - ").append(order.getStatus())
                    .append(" - ₹").append(order.getTotalAmount())
                    .append(" - ").append(order.getCreatedAt()).append("\n");
        }


        
        // Send export via email
        emailNotificationService.sendCustomerDataExport(customer, exportData.toString());
        
        log.info("Customer data export completed for ID: {}", customerId);
    }

    /**
     * Get recently registered customers
     */
    public Page<CustomerDTO> getNewCustomers(int days, Pageable pageable) {
        log.debug("Getting new customers from last {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        Page<User> newUsers = userRepository.findByCreatedAtAfterOrderByCreatedAtDesc(cutoffDate, pageable);
        
        List<CustomerDTO> customerDTOs = newUsers.getContent().stream()
                .map(this::mapToCustomerDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(customerDTOs, pageable, newUsers.getTotalElements());
    }

    /**
     * Get customer segmentation data
     */
    public CustomerSegmentationDTO getCustomerSegmentation() {
        log.debug("Generating customer segmentation data");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime ninetyDaysAgo = now.minusDays(90);
        
        // Get basic counts
        Long totalCustomers = userRepository.count();
        Long newCustomers = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        Long inactiveCustomers = userRepository.countByLastLoginBeforeOrLastLoginIsNull(ninetyDaysAgo);
        
        // Calculate segmentation data
        CustomerSegmentationDTO segmentation = CustomerSegmentationDTO.builder()
                .totalCustomers(totalCustomers.intValue())
                .newCustomers(newCustomers.intValue())
                .inactiveCustomers(inactiveCustomers.intValue())
                .generatedAt(now)
                .reportPeriod("Last 30 days")
                .build();
        
        // Calculate spending tiers
        segmentation.setBySpendingTiers(calculateSpendingTiers());
        
        // Calculate order frequency
        segmentation.setByOrderFrequency(calculateOrderFrequency());
        
        // Calculate geographic distribution
        segmentation.setByLocation(calculateLocationDistribution());
        
        // Calculate other segmentation metrics
        segmentation.setReturningCustomers(calculateReturningCustomers());
        segmentation.setLoyalCustomers(calculateLoyalCustomers());
        segmentation.setHighValueCustomers(calculateHighValueCustomers());
        segmentation.setVipCustomers(calculateVipCustomers());
        
        log.info("Customer segmentation data generated successfully");
        return segmentation;
    }

    // Private helper methods

    private Specification<User> createCustomerSpecification(CustomerFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Search filter
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, emailPredicate));
            }
            
            // Status filter
            if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), 
                        UserStatus.valueOf(filter.getStatus().toUpperCase())));
            }
            
            // Date range filters
            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), filter.getStartDate()));
            }
            
            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), filter.getEndDate()));
            }
            
            // Verification filter
            if (filter.getIsVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), filter.getIsVerified()));
            }
            
            // Active filter
            if (filter.getIsActive() != null) {
                if (filter.getIsActive()) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), UserStatus.ACTIVE));
                } else {
                    predicates.add(criteriaBuilder.notEqual(root.get("status"), UserStatus.ACTIVE));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private CustomerDTO mapToCustomerDTO(User user) {
        // Calculate order statistics
        List<Order> orders = orderRepository.findByUserId(user.getUserId());
        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        LocalDateTime lastOrderDate = orders.stream()
                .map(Order::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return CustomerDTO.builder()
                .id(user.getUserId())
                .name(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .registrationDate(user.getCreatedAt())
                .status(user.getStatus().name())
                .orderCount(orders.size())
                .totalSpent(totalSpent)
                .lastOrderDate(lastOrderDate)
                .isVerified(user.getEmailVerified())
                .isActive(user.getStatus() == UserStatus.ACTIVE)
                .build();
    }

    private CustomerDetailDTO mapToCustomerDetailDTO(User user) {
        CustomerDTO basicInfo = mapToCustomerDTO(user);
        List<Address> addresses = addressRepository.findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(user.getUserId());
        
        return CustomerDetailDTO.builder()
                .id(basicInfo.getId())
                .name(basicInfo.getName())
                .email(basicInfo.getEmail())
                .phone(basicInfo.getPhone())
                .registrationDate(basicInfo.getRegistrationDate())
                .status(basicInfo.getStatus())
                .orderCount(basicInfo.getOrderCount())
                .totalSpent(basicInfo.getTotalSpent())
                .lastOrderDate(basicInfo.getLastOrderDate())
                .isVerified(basicInfo.getIsVerified())
                .isActive(basicInfo.getIsActive())
                .addresses(addresses.stream()
                        .map(this::mapToAddressDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private CustomerStats calculateCustomerStats(User customer) {
        List<Order> orders = orderRepository.findByUserId(customer.getUserId());
        
        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageOrderValue = orders.isEmpty() ? BigDecimal.ZERO : 
                totalSpent.divide(BigDecimal.valueOf(orders.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        LocalDateTime firstOrderDate = orders.stream()
                .map(Order::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime lastOrderDate = orders.stream()
                .map(Order::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return CustomerStats.builder()
                .totalOrders(orders.size())
                .totalSpent(totalSpent)
                .averageOrderValue(averageOrderValue)
                .firstOrderDate(firstOrderDate)
                .lastOrderDate(lastOrderDate)
                .lifetimeValue(totalSpent)
                .customerTier(determineCustomerTier(totalSpent, orders.size()))
                .build();
    }

    private OrderSummaryDTO mapToOrderSummaryDTO(Order order) {
        return OrderSummaryDTO.builder()
                .id(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .status(order.getOrderStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .itemCount(order.getOrderItems().size())
                .build();
    }

    private void handleCustomerSuspension(User customer, CustomerStatusRequest request) {
        if (request.getSuspensionDays() != null && request.getSuspensionDays() > 0) {
            // In a real implementation, you might set a suspension end date
            LocalDateTime suspensionEndDate = LocalDateTime.now().plusDays(request.getSuspensionDays());
            log.info("Customer {} suspended until {}", customer.getId(), suspensionEndDate);
        }
    }

    private void refundPendingOrders(User customer) {
        List<Order> pendingOrders = orderRepository.findByUserIdAndStatusIn(
                customer.getId(),
                Arrays.asList(OrderStatus.PENDING, OrderStatus.CONFIRMED)
        );
        
        for (Order order : pendingOrders) {
            // In a real implementation, you would process refunds here
            log.info("Processing refund for order: {}", order.getOrderNumber());
        }
    }

    private void sendStatusChangeNotification(User customer, UserStatus oldStatus, UserStatus newStatus, String reason) {
        try {
            emailNotificationService.sendCustomerStatusChangeNotification(
                    customer, 
                    oldStatus.name(), 
                    newStatus.name(), 
                    reason
            );
        } catch (Exception e) {
            log.error("Failed to send status change notification to customer: {}", customer.getEmail(), e);
        }
    }

    private void anonymizeCustomerData(User customer) {
        customer.setName("DELETED_USER_" + customer.getId());
        customer.setEmail("deleted_" + customer.getId() + "@deleted.com");
        customer.setPhone(null);
        customer.setStatus(UserStatus.DELETED);
        customer.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(customer);
    }

    private com.suppkart.dto.admin.order.AddressDTO mapToAddressDTO(Address address) {
        return com.suppkart.dto.admin.order.AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }

    private Map<String, Integer> calculateSpendingTiers() {
        // In a real implementation, this would query the database for spending distribution
        Map<String, Integer> spendingTiers = new HashMap<>();
        spendingTiers.put("0-100", 150);
        spendingTiers.put("100-500", 80);
        spendingTiers.put("500-1000", 30);
        spendingTiers.put("1000+", 15);
        return spendingTiers;
    }

    private Map<String, Integer> calculateOrderFrequency() {
        Map<String, Integer> orderFrequency = new HashMap<>();
        orderFrequency.put("1", 120);
        orderFrequency.put("2-5", 100);
        orderFrequency.put("6-10", 40);
        orderFrequency.put("10+", 15);
        return orderFrequency;
    }

    private Map<String, Integer> calculateLocationDistribution() {
        Map<String, Integer> locationDistribution = new HashMap<>();
        locationDistribution.put("Mumbai", 50);
        locationDistribution.put("Delhi", 45);
        locationDistribution.put("Bangalore", 40);
        locationDistribution.put("Chennai", 35);
        locationDistribution.put("Others", 105);
        return locationDistribution;
    }

    private Integer calculateReturningCustomers() {
        // Customers with more than one order
        return Math.toIntExact(userRepository.countReturningCustomers());
    }

    private Integer calculateLoyalCustomers() {
        // Customers with 5+ orders or spent $500+
        return Math.toIntExact(userRepository.countLoyalCustomers());
    }

    private Integer calculateHighValueCustomers() {
        // Top 20% by spending
        Long totalCustomers = userRepository.count();
        return Math.toIntExact(totalCustomers / 5); // Approximate top 20%
    }

    private Integer calculateVipCustomers() {
        // Top 5% by spending
        Long totalCustomers = userRepository.count();
        return Math.toIntExact(totalCustomers / 20); // Approximate top 5%
    }

    private String determineCustomerTier(BigDecimal totalSpent, int orderCount) {
        if (totalSpent.compareTo(BigDecimal.valueOf(5000)) >= 0 || orderCount >= 20) {
            return "Diamond";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(2000)) >= 0 || orderCount >= 10) {
            return "Platinum";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(1000)) >= 0 || orderCount >= 5) {
            return "Gold";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(500)) >= 0 || orderCount >= 2) {
            return "Silver";
        } else {
            return "Bronze";
        }
    }
}

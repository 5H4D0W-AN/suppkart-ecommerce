package com.suppkart.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.suppkart.dto.admin.customer.CustomerDTO;
import com.suppkart.dto.admin.customer.CustomerDetailDTO;
import com.suppkart.dto.admin.customer.CustomerFilterRequest;
import com.suppkart.dto.admin.customer.CustomerSegmentationDTO;
import com.suppkart.dto.admin.customer.CustomerStats;
import com.suppkart.dto.admin.order.AddressDTO;
import com.suppkart.dto.admin.order.OrderSummaryDTO;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.service.AdminCustomerService;

@ExtendWith(MockitoExtension.class)
public class AdminCustomerControllerTest {

    @Mock
    private AdminCustomerService adminCustomerService;

    @InjectMocks
    private AdminCustomerController adminCustomerController;

    private CustomerDTO customerDTO;
    private CustomerDetailDTO customerDetailDTO;
    private CustomerStats customerStats;
    private CustomerSegmentationDTO customerSegmentationDTO;
    private OrderSummaryDTO orderSummaryDTO;
    private Page<CustomerDTO> customerPage;

    @BeforeEach
    void setUp() {
        // Setup customer DTO
        customerDTO = CustomerDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .registrationDate(LocalDateTime.now().minusDays(30))
                .status("ACTIVE")
                .orderCount(5)
                .totalSpent(BigDecimal.valueOf(2500.00))
                .lastOrderDate(LocalDateTime.now().minusDays(5))
                .isVerified(true)
                .isActive(true)
                .customerTier("GOLD")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .build();

        // Setup customer detail DTO
        AddressDTO addressDTO = AddressDTO.builder()
                .id(1L)
                .street("123 Main St")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .postalCode("400001")
                .isDefault(true)
                .build();

        customerDetailDTO = CustomerDetailDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .registrationDate(LocalDateTime.now().minusDays(30))
                .status("ACTIVE")
                .orderCount(5)
                .totalSpent(BigDecimal.valueOf(2500.00))
                .lastOrderDate(LocalDateTime.now().minusDays(5))
                .isVerified(true)
                .isActive(true)
                .addresses(Arrays.asList(addressDTO))
                .customerTier("GOLD")
                .loyaltyPoints(250)
                .build();

        // Setup customer stats
        customerStats = CustomerStats.builder()
                .totalOrders(5)
                .totalSpent(BigDecimal.valueOf(2500.00))
                .averageOrderValue(BigDecimal.valueOf(500.00))
                .lifetimeValue(BigDecimal.valueOf(2500.00))
                .customerTier("GOLD")
                .firstOrderDate(LocalDateTime.now().minusDays(25))
                .lastOrderDate(LocalDateTime.now().minusDays(5))
                .wishlistItemCount(3)
                .reviewCount(2)
                .averageRating(4.5)
                .build();

        // Setup order summary DTO
        orderSummaryDTO = OrderSummaryDTO.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .status("DELIVERED")
                .totalAmount(BigDecimal.valueOf(500.00))
                .createdAt(LocalDateTime.now().minusDays(5))
                .itemCount(2)
                .build();

        // Setup customer segmentation DTO
        Map<String, Integer> spendingTiers = new HashMap<>();
        spendingTiers.put("0-100", 150);
        spendingTiers.put("100-500", 80);
        spendingTiers.put("500-1000", 30);
        spendingTiers.put("1000+", 15);

        customerSegmentationDTO = CustomerSegmentationDTO.builder()
                .totalCustomers(275)
                .newCustomers(25)
                .returningCustomers(150)
                .loyalCustomers(50)
                .inactiveCustomers(30)
                .highValueCustomers(55)
                .vipCustomers(14)
                .bySpendingTiers(spendingTiers)
                .generatedAt(LocalDateTime.now())
                .reportPeriod("Last 30 days")
                .build();

        // Setup customer page
        List<CustomerDTO> customers = Arrays.asList(customerDTO);
        customerPage = new PageImpl<>(customers, PageRequest.of(0, 20), 1);
    }

    @Test
    void getAllCustomers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = adminCustomerController.getAllCustomers(
                "john", "ACTIVE", null, null, null, null, null, null, 
                null, null, null, true, null, null, null, null, null, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customers retrieved successfully", response.getBody().getMessage());
        assertEquals(customerPage, response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getCustomerById_Success() {
        // Arrange
        Long customerId = 1L;
        when(adminCustomerService.getCustomerById(customerId)).thenReturn(customerDetailDTO);

        // Act
        ResponseEntity<ApiResponse<CustomerDetailDTO>> response = 
                adminCustomerController.getCustomerById(customerId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customer details retrieved successfully", response.getBody().getMessage());
        assertEquals(customerDetailDTO, response.getBody().getData());
        assertEquals("John Doe", response.getBody().getData().getName());
        assertEquals("john.doe@example.com", response.getBody().getData().getEmail());
        assertEquals(1, response.getBody().getData().getAddresses().size());

        verify(adminCustomerService).getCustomerById(customerId);
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Arrange
        Long customerId = 999L;
        when(adminCustomerService.getCustomerById(customerId))
                .thenThrow(new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerController.getCustomerById(customerId);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(adminCustomerService).getCustomerById(customerId);
    }

    @Test
    void getCustomerOrders_Success() {
        // Arrange
        Long customerId = 1L;
        List<OrderSummaryDTO> orders = Arrays.asList(orderSummaryDTO);
        when(adminCustomerService.getCustomerOrders(customerId)).thenReturn(orders);

        // Act
        ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> response = 
                adminCustomerController.getCustomerOrders(customerId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customer orders retrieved successfully", response.getBody().getMessage());
        assertEquals(orders, response.getBody().getData());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("ORD-001", response.getBody().getData().get(0).getOrderNumber());

        verify(adminCustomerService).getCustomerOrders(customerId);
    }

    @Test
    void getCustomerOrders_CustomerNotFound_ThrowsException() {
        // Arrange
        Long customerId = 999L;
        when(adminCustomerService.getCustomerOrders(customerId))
                .thenThrow(new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerController.getCustomerOrders(customerId);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(adminCustomerService).getCustomerOrders(customerId);
    }

    @Test
    void getCustomerStats_Success() {
        // Arrange
        Long customerId = 1L;
        when(adminCustomerService.getCustomerStats(customerId)).thenReturn(customerStats);

        // Act
        ResponseEntity<ApiResponse<CustomerStats>> response = 
                adminCustomerController.getCustomerStats(customerId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customer statistics retrieved successfully", response.getBody().getMessage());
        assertEquals(customerStats, response.getBody().getData());
        assertEquals(5, response.getBody().getData().getTotalOrders());
        assertEquals(BigDecimal.valueOf(2500.00), response.getBody().getData().getTotalSpent());
        assertEquals("SILVER", response.getBody().getData().getCustomerTier());

        verify(adminCustomerService).getCustomerStats(customerId);
    }

    @Test
    void getCustomerStats_CustomerNotFound_ThrowsException() {
        // Arrange
        Long customerId = 999L;
        when(adminCustomerService.getCustomerStats(customerId))
                .thenThrow(new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerController.getCustomerStats(customerId);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(adminCustomerService).getCustomerStats(customerId);
    }

    @Test
    void deleteCustomer_Success() {
        // Arrange
        Long customerId = 1L;
        doNothing().when(adminCustomerService).deleteCustomer(customerId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminCustomerController.deleteCustomer(customerId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customer deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(adminCustomerService).deleteCustomer(customerId);
    }

    @Test
    void deleteCustomer_WithActiveOrders_ThrowsException() {
        // Arrange
        Long customerId = 1L;
        doThrow(new IllegalStateException("Cannot delete customer with active orders. Complete or cancel orders first."))
                .when(adminCustomerService).deleteCustomer(customerId);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            adminCustomerController.deleteCustomer(customerId);
        });

        assertEquals("Cannot delete customer with active orders. Complete or cancel orders first.", 
                exception.getMessage());
        verify(adminCustomerService).deleteCustomer(customerId);
    }

    @Test
    void exportCustomerData_Success() {
        // Arrange
        Long customerId = 1L;
        doNothing().when(adminCustomerService).exportCustomerData(customerId);

        // Act
        ResponseEntity<ApiResponse<String>> response = adminCustomerController.exportCustomerData(customerId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customer data export initiated. You will receive an email with the data.", 
                response.getBody().getMessage());
        assertEquals("Export initiated", response.getBody().getData());

        verify(adminCustomerService).exportCustomerData(customerId);
    }

    @Test
    void exportCustomerData_CustomerNotFound_ThrowsException() {
        // Arrange
        Long customerId = 999L;
        doThrow(new ResourceNotFoundException("Customer not found with ID: " + customerId))
                .when(adminCustomerService).exportCustomerData(customerId);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerController.exportCustomerData(customerId);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(adminCustomerService).exportCustomerData(customerId);
    }

    @Test
    void getNewCustomers_Success() {
        // Arrange
        int days = 30;
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getNewCustomers(days, pageable)).thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getNewCustomers(days, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("New customers retrieved successfully", response.getBody().getMessage());
        assertEquals(customerPage, response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());

        verify(adminCustomerService).getNewCustomers(days, pageable);
    }

    @Test
    void getNewCustomers_DefaultDays_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getNewCustomers(30, pageable)).thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getNewCustomers(30, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("New customers retrieved successfully", response.getBody().getMessage());

        verify(adminCustomerService).getNewCustomers(30, pageable);
    }

    @Test
    void getCustomerSegmentation_Success() {
        // Arrange
        when(adminCustomerService.getCustomerSegmentation()).thenReturn(customerSegmentationDTO);

        // Act
        ResponseEntity<ApiResponse<CustomerSegmentationDTO>> response = 
                adminCustomerController.getCustomerSegmentation();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customer segmentation data retrieved successfully", response.getBody().getMessage());
        assertEquals(customerSegmentationDTO, response.getBody().getData());
        assertEquals(275, response.getBody().getData().getTotalCustomers());
        assertEquals(25, response.getBody().getData().getNewCustomers());
        assertNotNull(response.getBody().getData().getBySpendingTiers());

        verify(adminCustomerService).getCustomerSegmentation();
    }

    @Test
    void getCustomersByStatus_Success() {
        // Arrange
        String status = "ACTIVE";
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getCustomersByStatus(status, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customers with status ACTIVE retrieved successfully", response.getBody().getMessage());
        assertEquals(customerPage, response.getBody().getData());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getHighValueCustomers_Success() {
        // Arrange
        BigDecimal minSpent = BigDecimal.valueOf(10000);
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getHighValueCustomers(minSpent, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("High-value customers retrieved successfully", response.getBody().getMessage());
        assertEquals(customerPage, response.getBody().getData());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getHighValueCustomers_DefaultMinSpent_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getHighValueCustomers(BigDecimal.valueOf(10000), pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("High-value customers retrieved successfully", response.getBody().getMessage());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getAtRiskCustomers_Success() {
        // Arrange
        int inactiveDays = 90;
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getAtRiskCustomers(inactiveDays, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("At-risk customers retrieved successfully", response.getBody().getMessage());
        assertEquals(customerPage, response.getBody().getData());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getAtRiskCustomers_DefaultInactiveDays_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getAtRiskCustomers(90, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("At-risk customers retrieved successfully", response.getBody().getMessage());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getCustomersByLocation_Success() {
        // Arrange
        String city = "Mumbai";
        String state = "Maharashtra";
        String country = "India";
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getCustomersByLocation(city, state, country, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customers by location retrieved successfully", response.getBody().getMessage());
        assertEquals(customerPage, response.getBody().getData());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getCustomersByLocation_PartialLocation_Success() {
        // Arrange
        String city = "Mumbai";
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.getCustomersByLocation(city, null, null, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customers by location retrieved successfully", response.getBody().getMessage());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void searchCustomers_Success() {
        // Arrange
        String query = "john.doe@example.com";
        Pageable pageable = PageRequest.of(0, 20);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = 
                adminCustomerController.searchCustomers(query, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customer search results retrieved successfully", response.getBody().getMessage());
        assertEquals(customerPage, response.getBody().getData());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getAllCustomers_WithAllFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime startDate = LocalDateTime.now().minusDays(60);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime lastLoginBefore = LocalDateTime.now().minusDays(30);
        
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(customerPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = adminCustomerController.getAllCustomers(
                "john", "ACTIVE", startDate, endDate, 5, BigDecimal.valueOf(1000), 
                "GOLD", "LOW", "Mumbai", "Maharashtra", "India", true, 
                "ORGANIC", true, true, true, lastLoginBefore, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customers retrieved successfully", response.getBody().getMessage());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getAllCustomers_EmptyResult_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<CustomerDTO> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(adminCustomerService.getAllCustomers(any(CustomerFilterRequest.class), eq(pageable)))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<ApiResponse<Page<CustomerDTO>>> response = adminCustomerController.getAllCustomers(
                null, null, null, null, null, null, null, null, 
                null, null, null, null, null, null, null, null, null, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customers retrieved successfully", response.getBody().getMessage());
        assertEquals(0, response.getBody().getData().getTotalElements());

        verify(adminCustomerService).getAllCustomers(any(CustomerFilterRequest.class), eq(pageable));
    }

    @Test
    void getCustomerOrders_EmptyResult_Success() {
        // Arrange
        Long customerId = 1L;
        when(adminCustomerService.getCustomerOrders(customerId)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> response = 
                adminCustomerController.getCustomerOrders(customerId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Customer orders retrieved successfully", response.getBody().getMessage());
        assertEquals(0, response.getBody().getData().size());

        verify(adminCustomerService).getCustomerOrders(customerId);
    }
}
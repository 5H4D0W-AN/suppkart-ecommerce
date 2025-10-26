package com.suppkart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.jpa.domain.Specification;

import com.suppkart.dto.admin.customer.CustomerDTO;
import com.suppkart.dto.admin.customer.CustomerDetailDTO;
import com.suppkart.dto.admin.customer.CustomerFilterRequest;
import com.suppkart.dto.admin.customer.CustomerSegmentationDTO;
import com.suppkart.dto.admin.customer.CustomerStats;
import com.suppkart.dto.admin.order.OrderSummaryDTO;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Address;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.UserStatus;
import com.suppkart.repository.AddressRepository;
import com.suppkart.repository.OrderRepository;
import com.suppkart.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdminCustomerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private AdminCustomerService adminCustomerService;

    private User testUser;
    private Order testOrder;
    private Address testAddress;
    private CustomerFilterRequest filterRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhone("+1234567890");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(LocalDateTime.now().minusDays(30));
        testUser.setUpdatedAt(LocalDateTime.now());

        // Setup test order
        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setOrderNumber("ORD-001");
        testOrder.setUser(testUser);
        testOrder.setOrderStatus(OrderStatus.DELIVERED);
        testOrder.setTotalAmount(BigDecimal.valueOf(1500.00));
        testOrder.setCreatedAt(LocalDateTime.now().minusDays(10));

        // Setup test address
        testAddress = new Address();
        testAddress.setAddressId(1L);
        testAddress.setFirstName("John");
        testAddress.setLastName("Doe");
        testAddress.setAddressLine1("123 Main St");
        testAddress.setCity("Mumbai");
        testAddress.setState("Maharashtra");
        testAddress.setCountry("India");
        testAddress.setPostalCode("400001");
        testAddress.setIsDefault(true);
        testAddress.setUser(testUser);

        // Setup filter request
        filterRequest = CustomerFilterRequest.builder()
                .search("john")
                .status("ACTIVE")
                .isVerified(true)
                .build();
    }

    @Test
    void getAllCustomers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        List<Order> orders = Arrays.asList(testOrder);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(orderRepository.findByUserId(testUser.getUserId())).thenReturn(orders);

        // Act
        Page<CustomerDTO> result = adminCustomerService.getAllCustomers(filterRequest, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        CustomerDTO customerDTO = result.getContent().get(0);
        assertEquals(testUser.getUserId(), customerDTO.getId());
        assertEquals("John Doe", customerDTO.getName());
        assertEquals(testUser.getEmail(), customerDTO.getEmail());
        assertEquals(testUser.getPhone(), customerDTO.getPhone());
        assertEquals(testUser.getStatus().name(), customerDTO.getStatus());
        assertEquals(1, customerDTO.getOrderCount());
        assertEquals(BigDecimal.valueOf(1500.00), customerDTO.getTotalSpent());
        assertTrue(customerDTO.getIsVerified());
        assertTrue(customerDTO.getIsActive());

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(orderRepository).findByUserId(testUser.getUserId());
    }

    @Test
    void getCustomerById_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        List<Address> addresses = Arrays.asList(testAddress);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(testUser.getUserId())).thenReturn(orders);
        when(addressRepository.findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(testUser.getUserId()))
                .thenReturn(addresses);

        // Act
        CustomerDetailDTO result = adminCustomerService.getCustomerById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getPhone(), result.getPhone());
        assertEquals(testUser.getStatus().name(), result.getStatus());
        assertEquals(1, result.getOrderCount());
        assertEquals(BigDecimal.valueOf(1500.00), result.getTotalSpent());
        assertEquals(1, result.getAddresses().size());
        assertTrue(result.getIsVerified());
        assertTrue(result.getIsActive());

        verify(userRepository).findById(1L);
        verify(orderRepository).findByUserId(testUser.getUserId());
        verify(addressRepository).findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(testUser.getUserId());
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerService.getCustomerById(999L);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getCustomerOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(orders);

        // Act
        List<OrderSummaryDTO> result = adminCustomerService.getCustomerOrders(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        OrderSummaryDTO orderDTO = result.get(0);
        assertEquals(testOrder.getOrderId(), orderDTO.getId());
        assertEquals(testOrder.getOrderNumber(), orderDTO.getOrderNumber());
        assertEquals(testOrder.getOrderStatus().name(), orderDTO.getStatus());
        assertEquals(testOrder.getTotalAmount(), orderDTO.getTotalAmount());
        assertEquals(testOrder.getCreatedAt(), orderDTO.getCreatedAt());

        verify(userRepository).findById(1L);
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getCustomerOrders_CustomerNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerService.getCustomerOrders(999L);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getCustomerStats_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        // Act
        CustomerStats result = adminCustomerService.getCustomerStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalOrders());
        assertEquals(0, result.getTotalSpent().compareTo(BigDecimal.valueOf(1500.00)));
        assertEquals(0, result.getAverageOrderValue().compareTo(BigDecimal.valueOf(1500.00)));
        assertEquals(0, result.getLifetimeValue().compareTo(BigDecimal.valueOf(1500.00)));
        assertEquals("SILVER", result.getCustomerTier());
        assertNotNull(result.getFirstOrderDate());
        assertNotNull(result.getLastOrderDate());

        verify(userRepository).findById(1L);
        verify(orderRepository).findByUserId(1L);
    }

    @Test
    void getCustomerStats_CustomerNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerService.getCustomerStats(999L);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void deleteCustomer_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserIdAndStatusIn(eq(1L), anyList())).thenReturn(Arrays.asList());

        // Act
        adminCustomerService.deleteCustomer(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(orderRepository).findByUserIdAndStatusIn(eq(1L), anyList());
        verify(userRepository).save(testUser);
        
        // Verify user data was anonymized
        assertTrue(testUser.getName().startsWith("DELETED_USER_"));
        assertTrue(testUser.getEmail().startsWith("deleted_"));
        assertNull(testUser.getPhone());
        assertEquals(UserStatus.DELETED, testUser.getStatus());
    }

    @Test
    void deleteCustomer_WithActiveOrders_ThrowsException() {
        // Arrange
        Order activeOrder = new Order();
        activeOrder.setOrderStatus(OrderStatus.PENDING);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Arrays.asList(activeOrder));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            adminCustomerService.deleteCustomer(1L);
        });

        assertEquals("Cannot delete customer with active orders. Complete or cancel orders first.", 
                exception.getMessage());
        verify(userRepository).findById(1L);
        verify(orderRepository).findByUserIdAndStatusIn(eq(1L), anyList());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteCustomer_CustomerNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerService.deleteCustomer(999L);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void exportCustomerData_Success() {
        // Arrange
        List<OrderSummaryDTO> orders = Arrays.asList();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList());
        doNothing().when(emailNotificationService).sendCustomerDataExport(eq(testUser), anyString());

        // Act
        adminCustomerService.exportCustomerData(1L);

        // Assert
        verify(userRepository, times(2)).findById(1L); // Called twice in the service
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(1L);
        verify(emailNotificationService).sendCustomerDataExport(eq(testUser), anyString());
    }

    @Test
    void exportCustomerData_CustomerNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminCustomerService.exportCustomerData(999L);
        });

        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getNewCustomers_Success() {
        // Arrange
        int days = 30;
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<User> newUsers = Arrays.asList(testUser);
        Page<User> newUsersPage = new PageImpl<>(newUsers, pageable, 1);
        List<Order> orders = Arrays.asList(testOrder);

        when(userRepository.findByCreatedAtAfterOrderByCreatedAtDesc(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(newUsersPage);
        when(orderRepository.findByUserId(testUser.getUserId())).thenReturn(orders);

        // Act
        Page<CustomerDTO> result = adminCustomerService.getNewCustomers(days, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        CustomerDTO customerDTO = result.getContent().get(0);
        assertEquals(testUser.getUserId(), customerDTO.getId());
        assertEquals("John Doe", customerDTO.getName());

        verify(userRepository).findByCreatedAtAfterOrderByCreatedAtDesc(any(LocalDateTime.class), eq(pageable));
        verify(orderRepository).findByUserId(testUser.getUserId());
    }

    @Test
    void getCustomerSegmentation_Success() {
        // Arrange
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(20L);
        when(userRepository.countByLastLoginBeforeOrLastLoginIsNull(any(LocalDateTime.class))).thenReturn(15L);
        when(userRepository.countReturningCustomers()).thenReturn(60L);
        when(userRepository.countLoyalCustomers()).thenReturn(25L);

        // Act
        CustomerSegmentationDTO result = adminCustomerService.getCustomerSegmentation();

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getTotalCustomers());
        assertEquals(20, result.getNewCustomers());
        assertEquals(15, result.getInactiveCustomers());
        assertEquals(60, result.getReturningCustomers());
        assertEquals(25, result.getLoyalCustomers());
        assertEquals(20, result.getHighValueCustomers()); // 100/5
        assertEquals(5, result.getVipCustomers()); // 100/20
        assertNotNull(result.getBySpendingTiers());
        assertNotNull(result.getByOrderFrequency());
        assertNotNull(result.getByLocation());
        assertNotNull(result.getGeneratedAt());
        assertEquals("Last 30 days", result.getReportPeriod());

        verify(userRepository, times(3)).count(); // Called 3 times in the service
        verify(userRepository).countByCreatedAtAfter(any(LocalDateTime.class));
        verify(userRepository).countByLastLoginBeforeOrLastLoginIsNull(any(LocalDateTime.class));
        verify(userRepository).countReturningCustomers();
        verify(userRepository).countLoyalCustomers();
    }

    @Test
    void mapToCustomerDTO_WithNoOrders_Success() {
        // Arrange
        when(orderRepository.findByUserId(testUser.getUserId())).thenReturn(Arrays.asList());

        // Act - using getAllCustomers to test the mapping indirectly
        Pageable pageable = PageRequest.of(0, 20);
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);

        Page<CustomerDTO> result = adminCustomerService.getAllCustomers(filterRequest, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        CustomerDTO customerDTO = result.getContent().get(0);
        assertEquals(0, customerDTO.getOrderCount());
        assertEquals(BigDecimal.ZERO, customerDTO.getTotalSpent());
        assertNull(customerDTO.getLastOrderDate());

        verify(orderRepository).findByUserId(testUser.getUserId());
    }

    @Test
    void calculateCustomerStats_WithMultipleOrders_Success() {
        // Arrange
        Order order1 = new Order();
        order1.setTotalAmount(BigDecimal.valueOf(1000.00));
        order1.setCreatedAt(LocalDateTime.now().minusDays(20));

        Order order2 = new Order();
        order2.setTotalAmount(BigDecimal.valueOf(2000.00));
        order2.setCreatedAt(LocalDateTime.now().minusDays(10));

        List<Order> orders = Arrays.asList(order1, order2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        // Act
        CustomerStats result = adminCustomerService.getCustomerStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalOrders());
        assertEquals(0, result.getTotalSpent().compareTo(BigDecimal.valueOf(3000.00)));
        assertEquals(0, result.getAverageOrderValue().compareTo(BigDecimal.valueOf(1500.00)));
        assertEquals(0, result.getLifetimeValue().compareTo(BigDecimal.valueOf(3000.00)));
        assertEquals("SILVER", result.getCustomerTier());

        verify(userRepository).findById(1L);
        verify(orderRepository).findByUserId(1L);
    }

    @Test
    void determineCustomerTier_Diamond_Success() {
        // Arrange
        Order highValueOrder = new Order();
        highValueOrder.setTotalAmount(BigDecimal.valueOf(6000.00));
        highValueOrder.setCreatedAt(LocalDateTime.now().minusDays(5));
        List<Order> orders = Arrays.asList(highValueOrder);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        // Act
        CustomerStats result = adminCustomerService.getCustomerStats(1L);

        // Assert
        assertEquals("GOLD", result.getCustomerTier());
    }

    @Test
    void determineCustomerTier_Bronze_Success() {
        // Arrange
        Order lowValueOrder = new Order();
        lowValueOrder.setTotalAmount(BigDecimal.valueOf(100.00));
        lowValueOrder.setCreatedAt(LocalDateTime.now().minusDays(5));
        List<Order> orders = Arrays.asList(lowValueOrder);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        // Act
        CustomerStats result = adminCustomerService.getCustomerStats(1L);

        // Assert
        assertEquals("BRONZE", result.getCustomerTier());
    }

    @Test
    void exportCustomerData_WithOrders_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testOrder));
        doNothing().when(emailNotificationService).sendCustomerDataExport(eq(testUser), anyString());

        // Act
        adminCustomerService.exportCustomerData(1L);

        // Assert
        verify(userRepository, times(2)).findById(1L); // Called twice in the service
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(1L);
        verify(emailNotificationService).sendCustomerDataExport(eq(testUser), contains("Orders (1):"));
    }

    @Test
    void getAllCustomers_EmptyResult_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        // Act
        Page<CustomerDTO> result = adminCustomerService.getAllCustomers(filterRequest, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getNewCustomers_EmptyResult_Success() {
        // Arrange
        int days = 30;
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(userRepository.findByCreatedAtAfterOrderByCreatedAtDesc(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(emptyPage);

        // Act
        Page<CustomerDTO> result = adminCustomerService.getNewCustomers(days, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        verify(userRepository).findByCreatedAtAfterOrderByCreatedAtDesc(any(LocalDateTime.class), eq(pageable));
    }
}
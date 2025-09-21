package com.suppkart.service;

import com.suppkart.dto.admin.*;
import com.suppkart.model.entity.Order;
import com.suppkart.model.entity.OrderItem;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.RoleType;
import com.suppkart.repository.OrderRepository;
import com.suppkart.repository.ProductRepository;
import com.suppkart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardSummaryDTO getDashboardSummary() {
        log.info("Fetching dashboard summary metrics");
        
        List<Order> allOrders = orderRepository.findAll();
        
        // Calculate total revenue from completed orders
        BigDecimal totalRevenue = allOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count total orders
        int orderCount = allOrders.size();

        // Count total customers (users with CUSTOMER role)
        int customerCount = (int) userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleType.CUSTOMER))
                .count();

        // Calculate average order value
        BigDecimal averageOrderValue = orderCount > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Count low stock products (products with variants having stock < 10)
        int lowStockCount = (int) productRepository.findAll().stream()
                .filter(product -> product.getVariants().stream()
                        .anyMatch(variant -> variant.getStockQuantity() < 10))
                .count();

        // Count pending orders
        int pendingOrdersCount = (int) allOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.PENDING || 
                               order.getOrderStatus() == OrderStatus.PROCESSING)
                .count();

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalRevenue(totalRevenue);
        summary.setOrderCount(orderCount);
        summary.setCustomerCount(customerCount);
        summary.setAverageOrderValue(averageOrderValue);
        summary.setLowStockCount(lowStockCount);
        summary.setPendingOrdersCount(pendingOrdersCount);
        
        return summary;
    }

    public List<SalesMetricDTO> getSalesTrend(int days) {
        log.info("Fetching sales trend for last {} days", days);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .filter(order -> {
                    LocalDate orderDate = order.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate()));

        return ordersByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Order> dayOrders = entry.getValue();
                    BigDecimal revenue = dayOrders.stream()
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    SalesMetricDTO metric = new SalesMetricDTO();
                    metric.setDate(date);
                    metric.setRevenue(revenue);
                    metric.setOrderCount(dayOrders.size());
                    return metric;
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    public List<OrderStatusCountDTO> getOrderStatusDistribution() {
        log.info("Fetching order status distribution");
        
        Map<OrderStatus, Long> statusCounts = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        return statusCounts.entrySet().stream()
                .map(entry -> {
                    OrderStatusCountDTO dto = new OrderStatusCountDTO();
                    dto.setStatus(entry.getKey().name());
                    dto.setCount(entry.getValue().intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<TopSellingProductDTO> getTopSellingProducts(int limit) {
        log.info("Fetching top {} selling products", limit);
        
        List<Order> deliveredOrders = orderRepository.findAll().stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        // Group order items by product and calculate sales
        Map<Long, List<OrderItem>> itemsByProduct = deliveredOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(item -> item.getProduct().getProductId()));

        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    List<OrderItem> items = entry.getValue();
                    
                    Product product = items.get(0).getProduct();
                    int unitsSold = items.stream().mapToInt(OrderItem::getQuantity).sum();
                    BigDecimal revenue = items.stream()
                            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    String thumbnail = product.getImages().isEmpty() ? "" : 
                            product.getImages().get(0).getImageUrl();
                    
                    TopSellingProductDTO dto = new TopSellingProductDTO();
                    dto.setId(productId);
                    dto.setName(product.getName());
                    dto.setSku(product.getSku());
                    dto.setUnitsSold(unitsSold);
                    dto.setRevenue(revenue);
                    dto.setThumbnail(thumbnail);
                    return dto;
                })
                .sorted((a, b) -> Integer.compare(b.getUnitsSold(), a.getUnitsSold()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<LowStockProductDTO> getLowStockProducts(int limit) {
        log.info("Fetching {} products with low stock", limit);
        
        return productRepository.findAll().stream()
                .filter(product -> product.getVariants().stream()
                        .anyMatch(variant -> variant.getStockQuantity() < 10))
                .map(product -> {
                    // Get minimum stock from all variants
                    int minStock = product.getVariants().stream()
                            .mapToInt(variant -> variant.getStockQuantity())
                            .min()
                            .orElse(0);
                    
                    String thumbnail = product.getImages().isEmpty() ? "" : 
                            product.getImages().get(0).getImageUrl();
                    
                    LowStockProductDTO dto = new LowStockProductDTO();
                    dto.setId(product.getProductId());
                    dto.setName(product.getName());
                    dto.setSku(product.getSku());
                    dto.setCurrentStock(minStock);
                    dto.setThumbnail(thumbnail);
                    return dto;
                })
                .sorted((a, b) -> Integer.compare(a.getCurrentStock(), b.getCurrentStock()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<RecentOrderDTO> getRecentOrders(int limit) {
        log.info("Fetching {} recent orders", limit);
        
        return orderRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .map(order -> {
                    User customer = order.getUser();
                    String customerName = customer.getFirstName() + " " + customer.getLastName();
                    
                    RecentOrderDTO dto = new RecentOrderDTO();
                    dto.setId(order.getOrderId());
                    dto.setOrderNumber(order.getOrderNumber());
                    dto.setCustomerName(customerName);
                    dto.setDate(order.getCreatedAt());
                    dto.setTotal(order.getTotalAmount());
                    dto.setStatus(order.getOrderStatus().name());
                    dto.setPaymentStatus(order.getPaymentStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public CustomerMetricsDTO getCustomerMetrics() {
        log.info("Fetching customer metrics");
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        // Count new customers (registered in last 30 days)
        int newCustomers = (int) userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleType.CUSTOMER))
                .filter(user -> user.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

        // Count returning customers (customers with more than one order)
        Map<Long, Long> orderCountByCustomer = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(order -> order.getUser().getUserId(), Collectors.counting()));
        
        int returningCustomers = (int) orderCountByCustomer.values().stream()
                .filter(count -> count > 1)
                .count();

        // Calculate conversion rate (orders / total customers)
        long totalCustomers = userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleType.CUSTOMER))
                .count();
        
        long totalOrders = orderRepository.count();
        double conversionRate = totalCustomers > 0 
                ? (double) totalOrders / totalCustomers * 100
                : 0.0;

        CustomerMetricsDTO dto = new CustomerMetricsDTO();
        dto.setNewCustomers(newCustomers);
        dto.setReturningCustomers(returningCustomers);
        dto.setConversionRate(conversionRate);
        return dto;
    }
}

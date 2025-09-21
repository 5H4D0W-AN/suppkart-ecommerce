package com.suppkart.service;

import com.suppkart.dto.admin.order.OrderDetailDTO;
import com.suppkart.dto.admin.order.ShipmentResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating PDF documents
 * Handles invoice generation and shipping label creation
 */
@Service
public class PDFGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(PDFGenerationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Generate PDF invoice for an order
     * Note: This is a basic implementation. For production, consider using libraries like:
     * - iText PDF
     * - Apache PDFBox
     * - Flying Saucer (for HTML to PDF)
     */
    public byte[] generateInvoice(OrderDetailDTO order) {
        try {
            logger.info("Generating invoice for order: {}", order.getId());

            // Create a simple text-based invoice (in production, use proper PDF library)
            StringBuilder invoiceContent = new StringBuilder();
            
            // Header
            invoiceContent.append("SUPPKART - INVOICE\n");
            invoiceContent.append("===================\n\n");
            
            // Order Information
            invoiceContent.append("Invoice Number: INV-").append(order.getId()).append("\n");
            invoiceContent.append("Order Number: ").append(order.getOrderNumber()).append("\n");
            invoiceContent.append("Date: ").append(order.getDate().format(DATE_TIME_FORMATTER)).append("\n");
            invoiceContent.append("Status: ").append(order.getStatus()).append("\n\n");
            
            // Customer Information
            invoiceContent.append("BILL TO:\n");
            invoiceContent.append("--------\n");
            if (order.getCustomer() != null) {
                invoiceContent.append("Name: ").append(order.getCustomer().getName()).append("\n");
                invoiceContent.append("Email: ").append(order.getCustomer().getEmail()).append("\n");
                if (order.getCustomer().getPhone() != null) {
                    invoiceContent.append("Phone: ").append(order.getCustomer().getPhone()).append("\n");
                }
            }
            invoiceContent.append("\n");
            
            // Billing Address
            if (order.getBillingAddress() != null) {
                invoiceContent.append("BILLING ADDRESS:\n");
                invoiceContent.append("----------------\n");
                invoiceContent.append(order.getBillingAddress().getFullAddress()).append("\n\n");
            }
            
            // Shipping Address
            if (order.getShippingAddress() != null) {
                invoiceContent.append("SHIPPING ADDRESS:\n");
                invoiceContent.append("-----------------\n");
                invoiceContent.append(order.getShippingAddress().getFullAddress()).append("\n\n");
            }
            
            // Order Items
            invoiceContent.append("ORDER ITEMS:\n");
            invoiceContent.append("------------\n");
            invoiceContent.append(String.format("%-40s %8s %12s %12s\n", "Product", "Qty", "Price", "Total"));
            invoiceContent.append("------------------------------------------------------------------------\n");
            
            if (order.getItems() != null) {
                for (var item : order.getItems()) {
                    String productName = truncate(item.getProductName(), 40);
                    invoiceContent.append(String.format("%-40s %8d %12.2f %12.2f\n",
                            productName,
                            item.getQuantity(),
                            item.getPrice(),
                            item.getTotal()));
                }
            }
            
            invoiceContent.append("------------------------------------------------------------------------\n");
            
            // Order Summary
            invoiceContent.append(String.format("%-61s %12.2f\n", "Subtotal:", order.getSubtotal()));
            if (order.getDiscount() != null && order.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                invoiceContent.append(String.format("%-61s %12.2f\n", "Discount:", order.getDiscount().negate()));
            }
            if (order.getTax() != null && order.getTax().compareTo(BigDecimal.ZERO) > 0) {
                invoiceContent.append(String.format("%-61s %12.2f\n", "Tax:", order.getTax()));
            }
            if (order.getShippingCost() != null && order.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
                invoiceContent.append(String.format("%-61s %12.2f\n", "Shipping:", order.getShippingCost()));
            }
            invoiceContent.append("========================================================================\n");
            invoiceContent.append(String.format("%-61s %12.2f\n", "TOTAL:", order.getTotal()));
            invoiceContent.append("\n");
            
            // Payment Information
            invoiceContent.append("PAYMENT INFORMATION:\n");
            invoiceContent.append("--------------------\n");
            invoiceContent.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
            invoiceContent.append("Payment Status: ").append(order.getPaymentStatus()).append("\n");
            if (order.getTransactionId() != null) {
                invoiceContent.append("Transaction ID: ").append(order.getTransactionId()).append("\n");
            }
            invoiceContent.append("\n");
            
            // Shipment Information
            if (order.getShipment() != null) {
                invoiceContent.append("SHIPMENT INFORMATION:\n");
                invoiceContent.append("---------------------\n");
                invoiceContent.append("Courier: ").append(order.getShipment().getCourierCompany()).append("\n");
                invoiceContent.append("Tracking Number: ").append(order.getShipment().getTrackingNumber()).append("\n");
                if (order.getShipment().getEstimatedDeliveryDate() != null) {
                    invoiceContent.append("Estimated Delivery: ")
                            .append(order.getShipment().getEstimatedDeliveryDate().format(DATE_FORMATTER))
                            .append("\n");
                }
                invoiceContent.append("\n");
            }
            
            // Footer
            invoiceContent.append("Thank you for your business!\n");
            invoiceContent.append("For support, contact us at support@suppkart.com\n");

            // Convert to bytes (in production, use proper PDF generation)
            return invoiceContent.toString().getBytes("UTF-8");

        } catch (Exception e) {
            logger.error("Error generating invoice for order {}: ", order.getId(), e);
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

    /**
     * Generate shipping label PDF
     * Note: This is a basic implementation. For production, integrate with shipping providers
     */
    public byte[] generateShippingLabel(ShipmentResponseDTO shipment, OrderDetailDTO order) {
        try {
            logger.info("Generating shipping label for shipment: {}", shipment.getId());

            StringBuilder labelContent = new StringBuilder();
            
            // Header
            labelContent.append("SUPPKART - SHIPPING LABEL\n");
            labelContent.append("=========================\n\n");
            
            // Shipment Information
            labelContent.append("Shipment ID: ").append(shipment.getId()).append("\n");
            labelContent.append("Order Number: ").append(order.getOrderNumber()).append("\n");
            labelContent.append("Tracking Number: ").append(shipment.getTrackingNumber()).append("\n");
            labelContent.append("Courier: ").append(shipment.getCourierCompany()).append("\n");
            labelContent.append("Weight: ").append(shipment.getPackageWeight()).append(" kg\n");
            if (shipment.getEstimatedDeliveryDate() != null) {
                labelContent.append("Est. Delivery: ")
                        .append(shipment.getEstimatedDeliveryDate().format(DATE_FORMATTER))
                        .append("\n");
            }
            labelContent.append("\n");
            
            // From Address (Company)
            labelContent.append("FROM:\n");
            labelContent.append("-----\n");
            labelContent.append("SuppKart\n");
            labelContent.append("123 Business Street\n");
            labelContent.append("Business City, State 12345\n");
            labelContent.append("Phone: +1-234-567-8900\n\n");
            
            // To Address
            labelContent.append("TO:\n");
            labelContent.append("---\n");
            if (order.getShippingAddress() != null) {
                labelContent.append(order.getShippingAddress().getFullName()).append("\n");
                labelContent.append(order.getShippingAddress().getFullAddress()).append("\n");
            }
            if (order.getCustomer() != null && order.getCustomer().getPhone() != null) {
                labelContent.append("Phone: ").append(order.getCustomer().getPhone()).append("\n");
            }
            labelContent.append("\n");
            
            // Barcode placeholder (in production, generate actual barcode)
            labelContent.append("TRACKING BARCODE:\n");
            labelContent.append("*").append(shipment.getTrackingNumber()).append("*\n\n");
            
            // Instructions
            labelContent.append("HANDLING INSTRUCTIONS:\n");
            labelContent.append("- Handle with care\n");
            labelContent.append("- This side up\n");
            labelContent.append("- Fragile items inside\n\n");
            
            labelContent.append("Generated on: ").append(java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));

            return labelContent.toString().getBytes("UTF-8");

        } catch (Exception e) {
            logger.error("Error generating shipping label for shipment {}: ", shipment.getId(), e);
            throw new RuntimeException("Failed to generate shipping label", e);
        }
    }

    /**
     * Truncate string to specified length
     */
    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() <= length ? str : str.substring(0, length - 3) + "...";
    }
}

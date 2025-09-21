package com.suppkart.dto.admin.order;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for shipment information in admin order details
 */
public class ShipmentDTO {
    
    private Long id;
    private String courierCompany;
    private String trackingNumber;
    private String trackingUrl;
    private String labelUrl;
    private double packageWeight;
    private LocalDate estimatedDeliveryDate;
    private LocalDateTime shipmentDate;

    // Constructors
    public ShipmentDTO() {}

    public ShipmentDTO(Long id, String courierCompany, String trackingNumber, String trackingUrl,
                      String labelUrl, double packageWeight, LocalDate estimatedDeliveryDate,
                      LocalDateTime shipmentDate) {
        this.id = id;
        this.courierCompany = courierCompany;
        this.trackingNumber = trackingNumber;
        this.trackingUrl = trackingUrl;
        this.labelUrl = labelUrl;
        this.packageWeight = packageWeight;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.shipmentDate = shipmentDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourierCompany() {
        return courierCompany;
    }

    public void setCourierCompany(String courierCompany) {
        this.courierCompany = courierCompany;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getLabelUrl() {
        return labelUrl;
    }

    public void setLabelUrl(String labelUrl) {
        this.labelUrl = labelUrl;
    }

    public double getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(double packageWeight) {
        this.packageWeight = packageWeight;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDateTime getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(LocalDateTime shipmentDate) {
        this.shipmentDate = shipmentDate;
    }
}

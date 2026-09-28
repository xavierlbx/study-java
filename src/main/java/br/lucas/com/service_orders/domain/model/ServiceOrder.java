package br.lucas.com.service_orders.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ServiceOrder {

    private Long id;
    private String protocol;
    private String customerName;
    private String customerDocument;
    private ServiceOrderType type;
    private ServiceOrderStatus status;
    private LocalDate scheduledDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public void updateData(String customerName, String customerDocument, ServiceOrderType type,
                           LocalDate scheduledDate, String notes, LocalDateTime updatedAt) {
        this.customerName = customerName;
        this.customerDocument = customerDocument;
        this.type = type;
        this.scheduledDate = scheduledDate;
        this.notes = notes;
        this.updatedAt = updatedAt;
    }
}

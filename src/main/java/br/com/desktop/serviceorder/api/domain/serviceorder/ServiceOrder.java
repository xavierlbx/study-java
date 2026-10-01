package br.com.desktop.serviceorder.api.domain.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.exception.InvalidScheduledDateException;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.InvalidStatusTransitionException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.CANCELED;
import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.DONE;
import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.IN_PROGRESS;
import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.OPEN;
import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.SCHEDULED;

public class ServiceOrder {

    private static final Map<ServiceOrderStatus, Set<ServiceOrderStatus>> VALID_TRANSITIONS = Map.of(
            OPEN, Set.of(SCHEDULED, CANCELED),
            SCHEDULED, Set.of(IN_PROGRESS, CANCELED),
            IN_PROGRESS, Set.of(DONE, CANCELED),
            DONE, Set.of(),
            CANCELED, Set.of()
    );

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

    public ServiceOrder(Long id, String protocol, String customerName, String customerDocument,
                        ServiceOrderType type, ServiceOrderStatus status, LocalDate scheduledDate, String notes,
                        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.protocol = protocol;
        this.customerName = customerName;
        this.customerDocument = customerDocument;
        this.type = type;
        this.status = status;
        this.scheduledDate = scheduledDate;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .protocol(protocol)
                .customerName(customerName)
                .customerDocument(customerDocument)
                .type(type)
                .status(status)
                .scheduledDate(scheduledDate)
                .notes(notes)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt);
    }

    public void updateData(String customerName, String customerDocument, ServiceOrderType type,
                           LocalDate scheduledDate, String notes, LocalDateTime updatedAt) {
        this.customerName = customerName;
        this.customerDocument = customerDocument;
        this.type = type;
        this.scheduledDate = scheduledDate;
        this.notes = notes;
        this.updatedAt = updatedAt;
    }

    public void changeStatusTo(ServiceOrderStatus newStatus, LocalDate scheduledDate, LocalDateTime now) {
        if (!VALID_TRANSITIONS.get(status).contains(newStatus)) {
            throw new InvalidStatusTransitionException(status, newStatus);
        }
        if (newStatus == SCHEDULED) {
            if (scheduledDate == null) {
                throw new InvalidScheduledDateException("Data de agendamento e obrigatoria para o status SCHEDULED.");
            }
            if (scheduledDate.isBefore(now.toLocalDate())) {
                throw new InvalidScheduledDateException("Data de agendamento nao pode ser no passado: " + scheduledDate + ".");
            }
            this.scheduledDate = scheduledDate;
        }
        this.status = newStatus;
        this.updatedAt = now;
    }

    public void markAsDeleted(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.updatedAt = deletedAt;
    }

    public Long getId() {
        return id;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerDocument() {
        return customerDocument;
    }

    public ServiceOrderType getType() {
        return type;
    }

    public ServiceOrderStatus getStatus() {
        return status;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public static class Builder {

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

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder customerDocument(String customerDocument) {
            this.customerDocument = customerDocument;
            return this;
        }

        public Builder type(ServiceOrderType type) {
            this.type = type;
            return this;
        }

        public Builder status(ServiceOrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder scheduledDate(LocalDate scheduledDate) {
            this.scheduledDate = scheduledDate;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public ServiceOrder build() {
            return new ServiceOrder(id, protocol, customerName, customerDocument, type, status, scheduledDate, notes,
                    createdAt, updatedAt, deletedAt);
        }
    }
}

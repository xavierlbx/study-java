package br.lucas.com.service_orders.domain.model;

import br.lucas.com.service_orders.domain.exception.InvalidStatusTransitionException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static br.lucas.com.service_orders.domain.model.ServiceOrderStatus.CANCELED;
import static br.lucas.com.service_orders.domain.model.ServiceOrderStatus.DONE;
import static br.lucas.com.service_orders.domain.model.ServiceOrderStatus.IN_PROGRESS;
import static br.lucas.com.service_orders.domain.model.ServiceOrderStatus.OPEN;
import static br.lucas.com.service_orders.domain.model.ServiceOrderStatus.SCHEDULED;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
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
            this.scheduledDate = scheduledDate;
        }
        this.status = newStatus;
        this.updatedAt = now;
    }

    public void markAsDeleted(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.updatedAt = deletedAt;
    }
}

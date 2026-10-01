package br.com.desktop.serviceorder.api.domain.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.exception.InvalidScheduledDateException;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.InvalidStatusTransitionException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.CANCELED;
import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.DONE;
import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.IN_PROGRESS;
import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.OPEN;
import static br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus.SCHEDULED;

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
}
